package org.example.ai.mlflow.fluent.attrubute.handlers

import org.example.ai.mlflow.fluent.processor.SpanAttributeHandler
import org.mlflow_project.google.gson.Gson
import java.lang.reflect.Method

object FunctionSpanAttributeHandler : SpanAttributeHandler {
    override fun processInput(method: Method, args: Array<Any?>): String {
        val inputMap = method.parameters.mapIndexed { index, parameter ->
            val paramName = parameter.name ?: "arg$index"
            val argumentValue = args.getOrNull(index) ?: "null"
            paramName to argumentValue
        }.toMap()
        return Gson().toJson(inputMap)
    }

    override fun processOutput(result: Any?): String = result.toString()
}
