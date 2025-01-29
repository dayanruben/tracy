package org.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.example.ai.features.haiku.generateHaiku


suspend fun main() {
    val response = generateHaiku("table")
    println(response)
}

suspend fun logExperiment() {
    val client = HttpClient(CIO)

    val response: HttpResponse = client.get("http://localhost:5000/api/2.0/mlflow/experiments/get") {
        setBody("""{"experiment_id": "549287403011006509"}""")
    }

    println(response.bodyAsText())
    client.close()
}