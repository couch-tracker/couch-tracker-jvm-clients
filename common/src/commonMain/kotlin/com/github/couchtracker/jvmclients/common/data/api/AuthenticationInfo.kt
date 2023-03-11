package com.github.couchtracker.jvmclients.common.data.api

import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.data.Database
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationToken(
    val token: String,
    val expiration: Instant,
)

@Serializable
data class AuthenticationInfo(
    val accessToken: AuthenticationToken,
    val refreshToken: AuthenticationToken,
    val user: User,
) {
    fun save(connection: CouchTrackerConnection, database: Database) {
        database.userCacheQueries.upsert(
            user = user,
            downloadTime = Clock.System.now(),
            connection = connection,
            id = user.id,
        )
        database.couchTrackerCredentialsQueries.upsert(
            connection = connection,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
