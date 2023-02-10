package com.github.couchtracker.jvmclients.common.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

data class CouchTrackerServer(
    val address: String,
) {
    suspend fun info(): CouchTrackerServerInfo {
        return client.get("$address/api").body<CouchTrackerServerInfo>()
    }

    suspend fun login(login: String, password: String): CouchTrackerConnection {
        @Serializable
        data class LoginRequest(val login: String, val password: String)

        @Serializable
        data class LoginResponse(val token: String, val refreshToken: String)

        val accessInfo = client.post("$address/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(login, password))
        }.body<LoginResponse>()
        return CouchTrackerConnection(
            this, login, accessInfo.token, accessInfo.refreshToken
        )
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