package ai.dev.kit.tracing.autologging

import ai.dev.kit.instrument
import ai.dev.kit.tracing.BaseOpenTelemetryTracingTest
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes.GEN_AI_REQUEST_MODEL
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes.GEN_AI_SYSTEM
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TracingTest : BaseOpenTelemetryTracingTest() {
    @Test
    fun testOpenAITracing() {
        val openAIClient = instrument(
            createLiteLLMClient()
        )

        val model = "openai/gpt-4o-mini"
        val systemMessage = "You are a helpful assistant!"
        val userMessage = "Tell me what model are you!"
        val temperature = 1.0

        val params = ChatCompletionCreateParams.builder()
            .addSystemMessage(systemMessage)
            .addUserMessage(userMessage)
            .model(model)
            .temperature(temperature)
            .build()

        val result = openAIClient.chat().completions().create(params)

        println(result.choices().first().message().content().get())

        analyzeSpans { spans ->
            assertTrue("Exactly one span is created") { spans.size == 1 }

            val span = spans.first()
            val attributes = span.attributes.asMap()

            assertTrue { span.name == "OpenAI-generation" }
            assertEquals(model, attributes[GEN_AI_REQUEST_MODEL])
            assertEquals("openai", attributes[GEN_AI_SYSTEM])
            assertEquals("https://litellm.labs.jb.gg", attributes[AttributeKey.stringKey("gen_ai.openai.api_base")])
            assertEquals("system", attributes[AttributeKey.stringKey("gen_ai.prompt.0.role")])
            assertEquals("\"$systemMessage\"", attributes[AttributeKey.stringKey("gen_ai.prompt.0.content")])
            assertEquals("user", attributes[AttributeKey.stringKey("gen_ai.prompt.1.role")])
            assertEquals("\"$userMessage\"", attributes[AttributeKey.stringKey("gen_ai.prompt.1.content")])
            assertEquals(24L, attributes[AttributeKey.longKey("gen_ai.usage.input_tokens")])
            assertEquals(temperature, attributes[AttributeKey.doubleKey("gen_ai.request.temperature")] as Double, absoluteTolerance = 0.00001)


            assertEquals("assistant", attributes[AttributeKey.stringKey("gen_ai.completion.0.role")])
            assertEquals("stop", attributes[AttributeKey.stringKey("gen_ai.completion.0.finish_reason")])


            assertTrue { attributes[AttributeKey.stringKey("gen_ai.completion.0.content")].toString().isNotEmpty() }
            assertTrue { attributes[AttributeKey.longKey("gen_ai.usage.output_tokens")] as Long > 0 }
        }
    }
}

private fun createLiteLLMClient(): OpenAIClient {
    return OpenAIOkHttpClient.builder()
        .baseUrl("https://litellm.labs.jb.gg")
        .apiKey(System.getenv("LITELLM_API_KEY") ?: error("LITELLM_API_KEY environment variable is not set"))
        .timeout(Duration.ofSeconds(60))
        .build()
}