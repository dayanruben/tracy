package org.example.ai.mlflow.fluent

import org.example.ai.mlflow.MlflowClients
import org.example.ai.mlflow.fluent.processor.TracingFlowProcessor

class MyClass {
    @KotlinFlowTrace(name = "Main Span", spanType = "func")
    fun computeResult(a: Int, b: Int, c: Int): Int {
        Thread.sleep(20)
        val result1 = performOperation1(a, b)
        val result2 = performOperation2(b, c)
        return combineResults(result1, result2)
    }

    @KotlinFlowTrace
    private fun performOperation1(x: Int, y: Int): Int {
        Thread.sleep(20)
        val intermediate1 = transformA(x)
        val intermediate2 = transformB(y)
        return intermediate1 + intermediate2
    }

    @KotlinFlowTrace
    protected fun performOperation2(x: Int, z: Int): Int {
        Thread.sleep(20)
        val intermediate1 = transformC(x, z)
        val intermediate2 = transformB(z)
        return intermediate1 * intermediate2
    }

    @KotlinFlowTrace(name="Multiply 2")
    private fun transformA(a: Int): Int {
        Thread.sleep(20)
        return a * 2
    }

    @KotlinFlowTrace
    open fun transformB(b: Int): Int {
        Thread.sleep(20)
        return b + 5
    }

    @KotlinFlowTrace
    open fun combineResults(r1: Int, r2: Int): Int {
        Thread.sleep(20)
        return r1 + r2 + transformC(7, 8)
    }


    companion object {
        @KotlinFlowTrace
        open fun transformC(c: Int, d: Int): Int {
            Thread.sleep(20)
            return c - d
        }
    }
}

fun main() {
    TracingFlowProcessor.setup()
    MlflowClients.setExperimentByName("TestAll")
    println(MyClass().computeResult(1, 2, 3))
}
