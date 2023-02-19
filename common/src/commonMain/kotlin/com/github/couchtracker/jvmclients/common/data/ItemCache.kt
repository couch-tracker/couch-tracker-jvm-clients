package com.github.couchtracker.jvmclients.common.data

import com.github.couchtracker.jvmclients.common.data.api.User
import com.github.couchtracker.jvmclients.common.utils.elapsedTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KotlinLogging
import kotlin.time.Duration.Companion.seconds

sealed class CachedValue<out T : Any> {
    object Loading : CachedValue<Nothing>()
    data class Error(val error: Throwable) : CachedValue<Nothing>()
    data class Loaded<T : Any>(val data: T, val downloadTime: Instant) : CachedValue<T>()
}

fun UserCache.toCachedValue(): CachedValue.Loaded<User> = CachedValue.Loaded(user, downloadTime)

private val logger = KotlinLogging.logger {}

class ItemCache<T : Any>(
    val dbFlow: Flow<CachedValue.Loaded<T>?>,
    val save: suspend (CachedValue.Loaded<T>) -> Unit,
    val download: suspend () -> T,
) {
    val data: Flow<CachedValue<T>> = dbFlow.transformLatest { dbCachedValue ->
        // TODO: re-download when old
        if (dbCachedValue == null) {
            emit(CachedValue.Loading)
            while (true) {
                logger.debug { "Item not found in database, downloading" }
                try {
                    val now = Clock.System.now()
                    val downloaded = logger.elapsedTime("Downloading") {
                        CachedValue.Loaded(download(), now)
                    }
                    logger.elapsedTime("Saving") {
                        save(downloaded)
                    }
                    break
                } catch (e: Throwable) {
                    if (e !is CancellationException) {
                        logger.error(e) { "Error while downloading item" }
                        // TODO: better retry strategy
                        emit(CachedValue.Error(e))
                        delay(5.seconds)
                    } else break
                }
            }
        } else {
            logger.debug { "Item found in database, downloaded at ${dbCachedValue.downloadTime}" }
            emit(CachedValue.Loaded(dbCachedValue.data, dbCachedValue.downloadTime))
        }
    }
}