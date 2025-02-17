package org.example.ai.mlflow.fluent

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import com.google.inject.matcher.Matchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.example.ai.mlflow.trace
import org.mlflow.tracking.MlflowClient
import java.util.*


@BindingAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME) // required for Guice
annotation class KotlinFlowTrace

class KotlinFlowTracer : MethodInterceptor {
    private val mlflowClient = MlflowClient("http://127.0.0.1:5000")

    @Throws(Throwable::class)
    override fun invoke(invocation: MethodInvocation): Any {
        val methodName = invocation.method.name
        println("Starting MLflow run for method: $methodName")

        // Start a new MLflow run
        val experimentId = mlflowClient.createExperiment("TestExperiment10")

        val result = invocation.proceed()

        runBlocking {
            val argumentsJson: JsonObject = buildJsonObject {
                invocation.arguments.forEachIndexed { index, arg ->
                    put("arg$index", JsonPrimitive(arg.toString()))
                }
            }

            trace(experimentId, tracingJson = buildJsonObject {
                put("request", argumentsJson)
                put("response", JsonPrimitive(result.toString()))
            })
        }

        return result
    }
}

class KotlinFlowTraceModule : AbstractModule() {
    override fun configure() {
        bindInterceptor(
            Matchers.any(),
            Matchers.annotatedWith(KotlinFlowTrace::class.java),
            KotlinFlowTracer()
        )
    }
}
