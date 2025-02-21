package org.example.ai.mlflow.fluent.processor

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.SuperCall
import org.example.ai.mlflow.MlflowClients
import org.example.ai.mlflow.createTrace
import org.example.ai.mlflow.dataclasses.TraceInfo
import org.example.ai.mlflow.dataclasses.createTracePostRequest
import org.example.ai.mlflow.fluent.KotlinFlowTrace
import java.lang.reflect.Method
import java.util.concurrent.Callable
import java.util.logging.LogManager
import java.util.logging.Logger

object TracedMethodInterceptor {
    private val tracer: Tracer = GlobalOpenTelemetry.getTracer("org.example.ai.mlflow")
    private val logger: Logger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME)
        ?: Logger.getLogger(TracedMethodInterceptor::class.java.name)

    @JvmStatic
    @RuntimeType
    fun intercept(@SuperCall originalMethod: Callable<*>,
                  @Origin method: Method,
                  @AllArguments args: Array<Any?>
    ): Any? {
        val span = createSpan(method)
        span.setAttribute("mlflow.spanInputs", extractInputs(method, args))

        val scope: Scope = span.makeCurrent()
        return try {
            val result = originalMethod.call()
            span.setAttribute("mlflow.spanOutputs", result.toString())
            result
        } catch (exception: Throwable) {
            span.recordException(exception)
            span.setStatus(StatusCode.ERROR, exception.message ?: "Unknown error")
            throw exception
        } finally {
            span.end()
            scope.close()
        }
    }

    private fun createSpan(method: Method): Span {
        val traceAnnotation = method.getAnnotation(KotlinFlowTrace::class.java)
        val spanName = traceAnnotation.name.ifBlank { method.name }

        val spanBuilder = tracer.spanBuilder(spanName)
        spanBuilder.setAttribute("mlflow.spanType", traceAnnotation.spanType)

        val parentSpan = Span.current()
        if (parentSpan.spanContext.isValid) {
            // If parent exists, set parent
            spanBuilder.setParent(Context.current())
        } else {
            // If root, then create a Trace and add traceCreationInfo to attribute
            spanBuilder.setNoParent()
            // TODO Get rid of run blocking
            runBlocking {
                val tracePostRequest = createTracePostRequest(
                    experimentId = MlflowClients.currentExperimentId,
                    traceCreationPath = method.declaringClass.name,
                    traceName = spanName
                )
                val traceInfo = createTrace(tracePostRequest)
                spanBuilder.setAttribute("traceCreationInfo", Json.encodeToString(TraceInfo.serializer(), traceInfo))
            }
        }
        return spanBuilder.startSpan()
    }

    private fun extractInputs(method: Method, args: Array<Any?>): String {
        val a = 3
        return method.parameters.mapIndexed { index, parameter ->
            val paramName = parameter.name ?: "arg$index"
            val argumentValue = args.getOrNull(index)?.toString() ?: "null"
            "\"$paramName\": $argumentValue"
        }.joinToString(", ", prefix = "{", postfix = "}")
    }
}
