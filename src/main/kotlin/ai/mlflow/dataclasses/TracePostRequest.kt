package org.example.ai.mlflow.dataclasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.ai.mlflow.getCurrentTimestamp

@Serializable
data class TracePostRequest(
    @SerialName("experiment_id") val experimentId: String,
    @SerialName("timestamp_ms") val timestampMs: Long = getCurrentTimestamp(),
    @SerialName("request_metadata") val requestMetadata: List<RequestMetadata>,
    @SerialName("tags") val tags: List<Tag>
)
