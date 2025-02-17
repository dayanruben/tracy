package org.example.ai.mlflow.fluent
import com.google.inject.Guice
import com.google.inject.Injector

open class MyClass {
    @KotlinFlowTrace
    open fun someFunction(x: Int, y: Int, z: Int = 2): Int {
        return x + (y - z)
    }
}

fun main() {
    val injector: Injector = Guice.createInjector(KotlinFlowTraceModule())
    val myClass = injector.getInstance(MyClass::class.java)
    println(myClass.someFunction(2, 4))
}
