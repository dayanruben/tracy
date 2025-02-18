package org.example.ai.mlflow.dataclasses

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class TracePatchTagRequest(
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("inputs") val inputs: String
) {
    override fun toString(): String {
        return Json.encodeToString(this)
    }
}