package org.example.ai.mlflow.dataclasses

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.trace.data.SpanData
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.example.ai.mlflow.Tag
import org.example.ai.mlflow.fluent.FluentSpanAttributes

fun List<SpanData>.toUpdateTraceTagsRequest() =
    Tag(
        key = "mlflow.traceSpans",
        value = Json.encodeToString(this.map { spanData ->
            UpdateTraceTagsRequest(
                name = spanData.name,
                type = "UNKNOWN",
                inputs = spanData.attributes[FluentSpanAttributes.MLFLOW_SPAN_INPUTS.asAttributeKey()]
            )
        })
    )

@Serializable
data class UpdateTraceTagsRequest(
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("inputs") val inputs: String?
) {
    override fun toString(): String {
        return Json.encodeToString(this)
    }
}