package com.github.couchtracker.jvmclients.common.data

import com.github.couchtracker.jvmclients.common.utils.elapsedTime
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
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
    f: HttpClientConfig<CIOEngineConfig>.() -> Unit = {},
): HttpClient {
    return logger.elapsedTime("Creating HTTP client") {
        HttpClient(CIO) {
            expectSuccess = true
            defaultRequest {
                url(server.address + "/api/")
            }
            install(ContentNegotiation) {
                json(defaultJson)
            }
            f()
        }
    }
}
