package org.example.ai.mlflow.fluent.processor

import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.SuperCall
import net.bytebuddy.matcher.ElementMatchers.any
import net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith
import net.bytebuddy.utility.JavaModule
import org.example.ai.mlflow.fluent.KotlinFlowTrace
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain
import java.util.concurrent.Callable

object TracingFlowDecoratorAgent {
    fun premain(arguments: String?, instrumentation: Instrumentation) {
        AgentBuilder.Default()
            .type(any()) // Match all classes
            .transform(object : AgentBuilder.Transformer {
                override fun transform(
                    builder: DynamicType.Builder<*>,
                    typeDescription: TypeDescription?,
                    classloader: ClassLoader?,
                    p3: JavaModule?,
                    p4: ProtectionDomain
                ): DynamicType.Builder<*> {
                    return builder
                        .method(isAnnotatedWith(KotlinFlowTrace::class.java))
                        .intercept(MethodDelegation.to(MethodInterceptor::class.java))
                }
            })
            .installOn(instrumentation)
    }
}

object MethodInterceptor {
    @JvmStatic
    @RuntimeType
    fun intercept(@SuperCall originalMethod: Callable<*>): Any? {
        println("Before method execution")
        val result = originalMethod.call()
        println("After method execution")
        return result
    }
}
