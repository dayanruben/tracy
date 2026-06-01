/*
 * Copyright © 2026 JetBrains s.r.o. and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.ai.tracy.gemini.adapters.handlers.cachedcontents.routes

import io.opentelemetry.api.trace.Span
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes.GEN_AI_OPERATION_NAME
import kotlinx.serialization.json.jsonObject
import org.jetbrains.ai.tracy.core.adapters.handlers.RouteHandler
import org.jetbrains.ai.tracy.core.http.protocol.TracyHttpRequest
import org.jetbrains.ai.tracy.core.http.protocol.TracyHttpResponse
import org.jetbrains.ai.tracy.core.http.protocol.asJson
import org.jetbrains.ai.tracy.gemini.adapters.handlers.cachedcontents.CachedContentTracer

/**
 * Handles the `GET /v1beta/cachedContents/{name}` endpoint.
 *
 * Request body is empty. Response body is a full `CachedContent` resource — traced via the
 * shared [CachedContentTracer].
 *
 * See: [Gemini Caching API — get](https://ai.google.dev/api/caching#method:-cachedcontents.get)
 */
internal class GetCachedContentHandler : RouteHandler {
    override fun handleRequest(span: Span, request: TracyHttpRequest) {
        span.setAttribute(GEN_AI_OPERATION_NAME, "get")
    }

    override fun handleResponse(span: Span, response: TracyHttpResponse) {
        response.body.asJson()?.jsonObject?.let { body ->
            CachedContentTracer.traceResponse(span, body)
        }
    }
}
