package com.github.couchtracker.jvmclients.common.data

import com.github.couchtracker.jvmclients.common.utils.elapsedTime
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import mu.KotlinLogging
import kotlinx.serialization.json.Json

private val defaultJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

private val logger = KotlinLogging.logger {}

fun couchTrackerHttpClient(
    server: CouchTrackerServer,
    f: HttpClientConfig<*>.() -> Unit = {},
): HttpClient {
    return logger.elapsedTime("Creating HTTP client") {
        HttpClient(OkHttp) {
            expectSuccess = true
            defaultRequest {
                url(server.address + "/api/")
            }
            //install(io.ktor.client.plugins.logging.Logging)
            install(ContentEncoding) {
                deflate(1.0F)
                gzip(0.9F)
            }
            install(ContentNegotiation) {
                json(defaultJson)
            }
            f()
        }
    }
}
