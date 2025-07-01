package ai.dev.kit.tracing

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import java.util.Base64
import java.util.concurrent.TimeUnit

const val AI_DEVELOPMENT_KIT_TRACER = "ai-development-kit"

fun setupLangfuseTracing(
    langfuseUrl: String,
    langfusePublicKey: String,
    langfuseSecretKey: String,
    traceToConsole: Boolean = false,
): SdkTracerProvider {
    val resource = Resource.getDefault()
        .merge(
            Resource.create(
                Attributes.of(AttributeKey.stringKey("service.name"), "ai-development-kit")
            )
        )

    val tracerProvider = SdkTracerProvider.builder()
        .setResource(resource)
        .addLangfuseSpanProcessor(langfuseUrl, langfusePublicKey, langfuseSecretKey)
        .apply {
            if (traceToConsole) {
                addLoggingSpanProcessor()
            }
        }
        .build()

    val openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .buildAndRegisterGlobal()

    val sdk = openTelemetry
    Runtime.getRuntime().addShutdownHook(Thread {
        sdk.sdkTracerProvider.shutdown()
    })

    return tracerProvider
}

private fun SdkTracerProviderBuilder.addLoggingSpanProcessor(): SdkTracerProviderBuilder {
    val spanExporter = LoggingSpanExporter.create()
    addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
    return this
}

fun SdkTracerProviderBuilder.addLangfuseSpanProcessor(
    langfuseUrl: String,
    langfusePublicKey: String,
    langfuseSecretKey: String,
): SdkTracerProviderBuilder {
    val credentials = "$langfusePublicKey:$langfuseSecretKey"
    val auth = Base64.getEncoder().encodeToString(credentials.toByteArray(Charsets.UTF_8))

    val otlpGrpcSpanExporter = OtlpHttpSpanExporter.builder()
        .setTimeout(30, TimeUnit.SECONDS)
        .setEndpoint("$langfuseUrl/api/public/otel/v1/traces")
        .addHeader("Authorization", "Basic $auth")
        .build()

    addSpanProcessor(
        BatchSpanProcessor.builder(otlpGrpcSpanExporter)
            .setScheduleDelay(3, TimeUnit.SECONDS)
            .build()
    )
    return this
}

