package com.github.couchtracker.jvmclients.common.data

data class CouchTrackerConnection(
    val server: CouchTrackerServer,
    val id: String,
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        suspend fun create(
            server: CouchTrackerServer,
            login: String,
            password: String,
        ): CouchTrackerConnection {
            return server.login(login, password)
        }
    }
}