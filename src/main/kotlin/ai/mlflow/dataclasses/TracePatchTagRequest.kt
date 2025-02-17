package org.example.ai.mlflow.dataclasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracePatchTagRequest(
    @SerialName("name") val name: String,
    @SerialName("type") val type: String = "UNKNOWN",
    @SerialName("inputs") val inputs: List<String>,
)
