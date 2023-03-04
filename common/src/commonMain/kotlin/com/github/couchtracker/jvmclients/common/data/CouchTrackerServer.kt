package com.github.couchtracker.jvmclients.common.data

import com.github.couchtracker.jvmclients.common.data.api.AuthenticationInfo
import com.github.couchtracker.jvmclients.common.data.api.CouchTrackerServerInfo
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = CouchTrackerServerSerializer::class)
data class CouchTrackerServer(
    val address: String,
) {
    private val client by lazy { couchTrackerHttpClient(this) }

    suspend fun info(): CouchTrackerServerInfo {
        return client.get("$address/api").body()
    }

    suspend fun login(login: String, password: String): AuthenticationInfo {
        @Serializable
        data class LoginRequest(val login: String, val password: String)

        return client.post("auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(login, password))
        }.body()
    }

    suspend fun refreshToken(refreshToken: String): AuthenticationInfo {
        return client.post("auth/refresh") {
            headers {
                bearerAuth(refreshToken)
            }
        }.body()
    }
}

class CouchTrackerServerSerializer : KSerializer<CouchTrackerServer> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CouchTrackerServer", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: CouchTrackerServer) {
        return encoder.encodeString(value.address)
    }

    override fun deserialize(decoder: Decoder): CouchTrackerServer {
        return CouchTrackerServer(decoder.decodeString())
    }
}
