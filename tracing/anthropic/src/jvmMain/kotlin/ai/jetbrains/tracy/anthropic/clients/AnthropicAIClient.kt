package ai.jetbrains.tracy.anthropic.clients

import ai.dev.kit.OpenTelemetryOkHttpInterceptor
import ai.dev.kit.patchOpenAICompatibleClient
import ai.jetbrains.tracy.anthropic.adapters.AnthropicLLMTracingAdapter
import com.anthropic.client.AnthropicClient

fun instrument(client: AnthropicClient): AnthropicClient {
    return patchOpenAICompatibleClient(
        client = client,
        interceptor = OpenTelemetryOkHttpInterceptor(adapter = AnthropicLLMTracingAdapter())
    )
}
