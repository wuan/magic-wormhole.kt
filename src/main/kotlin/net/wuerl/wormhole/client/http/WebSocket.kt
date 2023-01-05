package net.wuerl.wormhole.client.http

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*

suspend fun HttpClient.webSocket(
    targetUrl: Url, request: HttpRequestBuilder.() -> Unit = {}, block: suspend DefaultClientWebSocketSession.() -> Unit
): Unit = webSocket(
    HttpMethod.Get, targetUrl.host, targetUrl.port, targetUrl.fullPath, {
        url.protocol = targetUrl.protocol
        url.port = targetUrl.port
        request()
    }, block = block
)