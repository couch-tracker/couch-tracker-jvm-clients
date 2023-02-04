package com.github.couchtracker.jvmclients.common.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable

data class CouchTrackerServer(
    val address: String,
) {
    suspend fun info(): CouchTrackerServerInfo {
        return client.get("$address/api").body<CouchTrackerServerInfo>()
    }

    companion object {
        private val client = HttpClient(CIO) {
            expectSuccess = true
            install(ContentNegotiation) {
                json()
            }
        }
    }
}

@Serializable
data class CouchTrackerServerInfo(
    val version: Int,
    val patch: Int,
    val name: String,
) {
    init {
        require(name == "couch-tracker")
        require(version > 0)
        require(patch >= 0)
    }
}