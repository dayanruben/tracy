package org.example.ai.mlflow.dataclasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.ai.mlflow.getCurrentTimestamp
import java.time.Instant

internal fun createTracePostRequest(
    requestId: String,
    endEpochNanos: Long,
    rootInputs: String?,
    rootResult: String?
) = TracePatchRequest(
    requestId = requestId,
    status = "OK",
    timestampMs = endEpochNanos,
    requestMetadata = listOf(
        RequestMetadata("mlflow.trace_schema.version", "2"),
        RequestMetadata("mlflow.traceInputs", rootInputs ?: "null"),
        RequestMetadata("mlflow.traceOutputs", rootResult ?: "null")
    ),
    tags = listOf(
        Tag("mlflow.source.name", "some_function"),
        Tag("mlflow.source.type", "LOCAL"),
        Tag("mlflow.traceName", "some_function")
    )
)

@Serializable
data class TracePatchRequest(
    @SerialName("request_id") val requestId: String,
    @SerialName("timestamp_ms") val timestampMs: Long = getCurrentTimestamp(),
    @SerialName("status") val status: String,
    @SerialName("request_metadata") val requestMetadata: List<RequestMetadata>,
    @SerialName("tags") val tags: List<Tag>
)
