package com.github.couchtracker.jvmclients.common.data

import com.github.couchtracker.jvmclients.common.utils.elapsedTime
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging


private val defaultJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

private val logger = KotlinLogging.logger {}

fun couchTrackerHttpClient(
    server: CouchTrackerServer,
    f: HttpClientConfig<CIOEngineConfig>.() -> Unit = {}
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