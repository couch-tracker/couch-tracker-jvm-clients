package com.github.couchtracker.jvmclients.common.data

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.github.couchtracker.jvmclients.common.data.api.ShowBasicInfo
import com.github.couchtracker.jvmclients.common.data.api.User
import com.github.couchtracker.jvmclients.common.utils.elapsedTime
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import mu.KotlinLogging
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.runningFold

private val logger = KotlinLogging.logger {}

class CouchTrackerDataPortals(
    val portals: List<CouchTrackerDataPortal>,
) {
    val active = portals.firstOrNull()

    companion object {
        val empty = CouchTrackerDataPortals(emptyList())
        fun getInstance(database: Database): Flow<CouchTrackerDataPortals> {
            return database.couchTrackerCredentialsQueries.all()
                .asFlow()
                .mapToList(Dispatchers.Main)
                .runningFold(emptyMap<CouchTrackerConnection, CouchTrackerDataPortal>()) { previous, connections ->
                    connections.associateWith {
                        previous[it] ?: CouchTrackerDataPortal(it, database)
                    }
                }
                .mapLatest { CouchTrackerDataPortals(it.values.toList()) }
        }
    }
}

class CouchTrackerDataPortal(
    val connection: CouchTrackerConnection,
    val database: Database,
) {

    init {
        logger.debug { "Creating data portal for $connection" }
    }

    private val client: HttpClient = couchTrackerHttpClient(connection.server) {
        install(Auth) {
            bearer {
                sendWithoutRequest { true }
                loadTokens {
                    logger.elapsedTime("Reading credentials") {
                        credentials()
                    }
                }
                refreshTokens {
                    logger.elapsedTime("Refreshing credentials") {
                        val refreshToken = credentials()?.refreshToken ?: return@refreshTokens null
                        val new = try {
                            connection.server.refreshToken(refreshToken)
                        } catch (expected: Exception) {
                            logger.error(expected) { "Couldn't refresh access token" }
                            null
                        }
                        if (new == null) {
                            // TODO: prompt to re-login somehow
                            database.couchTrackerCredentialsQueries.deleteCredentials(connection)
                            null
                        } else {
                            database.couchTrackerCredentialsQueries.upsert(new.accessToken, new.refreshToken, connection)
                            BearerTokens(new.accessToken.token, new.refreshToken.token)
                        }
                    }
                }
            }
        }
    }

    private suspend fun credentials(): BearerTokens? {
        return database.couchTrackerCredentialsQueries
            .bearer(connection)
            .awaitAsOneOrNull()
            ?.let {
                if (it.accessToken != null && it.refreshToken != null) {
                    BearerTokens(it.accessToken.token, it.refreshToken.token)
                } else {
                    null
                }
            }
    }

    fun user(userId: String): Flow<CachedValue<User>> {
        // TODO: this should be cached
        return ItemCache(
            dbFlow = database.userCacheQueries
                .get(connection, userId)
                .asFlow()
                .mapToOneOrNull(Dispatchers.Main)
                .mapLatest { it?.toCachedValue() },
            save = {
                database.userCacheQueries.upsert(
                    id = userId,
                    connection = connection,
                    user = it.data,
                    downloadTime = it.downloadTime,
                )
            },
            download = { client.get("users/$userId").body() },
        ).data
    }

    suspend fun show(
        showId: String,
        locales: List<Locale> = listOf(Locale.ENGLISH),
    ): ShowBasicInfo {
        // TODO: do it like the user
        return client.get("shows/$showId") {
            // TODO: do better
            locales.forEach {
                parameter("locales", it.language)
            }
        }.body()
    }
}
