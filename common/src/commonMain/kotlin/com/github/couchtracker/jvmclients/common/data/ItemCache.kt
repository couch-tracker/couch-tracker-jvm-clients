@file:OptIn(ExperimentalCoroutinesApi::class)

package com.github.couchtracker.jvmclients.common.data

import com.github.couchtracker.jvmclients.common.utils.elapsedTime
import mu.KotlinLogging
import java.util.Locale
import java.util.Optional
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed class CachedValue<out T> {
    sealed class NotLoaded : CachedValue<Nothing>()
    object Loading : NotLoaded()
    data class Error(val error: Throwable, val downloadTime: Instant) : NotLoaded()
    data class Loaded<T>(val data: T, val downloadTime: Instant) : CachedValue<T>() {
        val expireDate = downloadTime.plus(1.days)
        val isUpToDate = Clock.System.now().minus(1.days) < expireDate
    }
}

private val logger = KotlinLogging.logger {}

data class LocalizedKey<K>(val locales: List<Locale>, val key: K)

class ItemsCache<K, T> private constructor(
    private val scope: CoroutineScope,
    private val load: suspend (key: K) -> Optional<CachedValue.Loaded<T>>,
    private val save: suspend (key: K, CachedValue.Loaded<T>) -> Unit,
    private val download: suspend (key: K) -> Result<T>,
) {
    private val cache = mutableMapOf<K, Flow<CachedValue<T>>>()
    fun get(key: K): Flow<CachedValue<T>> {
        return cache.getOrPut(key) {
            getItem(key)
        }
    }

    private fun getItem(key: K): Flow<CachedValue<T>> {
        val info = ItemCacheStateTransitionHandler(
            { load(key) },
            { save(key, it) },
            { download(key) },
        )
        return flow {
            var state: ItemCacheState<T> = ItemCacheState.Unknown(info)
            emit(state.data)
            while (true) {
                state = state.next()
                emit(state.data)
            }
        }
            .flowOn(Dispatchers.IO)
            .shareIn(scope, SharingStarted.WhileSubscribed(), 1)
    }

    companion object {
        fun <K, T> persistent(
            scope: CoroutineScope,
            load: suspend (key: K) -> Optional<CachedValue.Loaded<T>>,
            save: suspend (key: K, CachedValue.Loaded<T>) -> Unit,
            download: suspend (key: K) -> Result<T>,
        ): ItemsCache<K, T> {
            return ItemsCache(
                scope,
                { k ->
                    logger.elapsedTime("Loading $k from database") {
                        load(k)
                    }
                },
                { k, v ->
                    logger.elapsedTime("Saving $k to database") {
                        save(k, v)
                    }
                },
                { k ->
                    logger.elapsedTime("Downloading $k") {
                        download(k)
                    }
                },
            )
        }

        fun <K, T> volatile(
            scope: CoroutineScope,
            download: suspend (key: K) -> Result<T>,
        ): ItemsCache<K, T> {
            return ItemsCache(
                scope,
                { Optional.empty() },
                { _, _ -> },
                { k ->
                    logger.elapsedTime("Downloading $k") {
                        download(k)
                    }
                },
            )
        }
    }
}

private class ItemCacheStateTransitionHandler<T>(
    val load: suspend () -> Optional<CachedValue.Loaded<T>>,
    val save: suspend (CachedValue.Loaded<T>) -> Unit,
    val download: suspend () -> Result<T>,
)

private sealed class ItemCacheState<T>(val info: ItemCacheStateTransitionHandler<T>) {
    abstract val data: CachedValue<T>

    abstract suspend fun next(): ItemCacheState<T>

    class Unknown<T>(info: ItemCacheStateTransitionHandler<T>) : ItemCacheState<T>(info) {
        override val data get() = CachedValue.Loading

        override suspend fun next(): ItemCacheState<T> {
            val data = info.load()
            return if (data.isPresent) {
                if (data.get().isUpToDate) {
                    Updated(info, data.get())
                } else {
                    Refreshing(info, data.get())
                }
            } else {
                Refreshing(info, null)
            }
        }
    }

    class Updated<T>(info: ItemCacheStateTransitionHandler<T>, override val data: CachedValue.Loaded<T>) : ItemCacheState<T>(info) {
        override suspend fun next(): Refreshing<T> {
            delay(data.expireDate - Clock.System.now())
            return Refreshing(info, data)
        }
    }

    class Saving<T>(info: ItemCacheStateTransitionHandler<T>, override val data: CachedValue.Loaded<T>) : ItemCacheState<T>(info) {
        override suspend fun next(): Updated<T> {
            info.save(data)
            return Updated(info, data)
        }
    }

    class Refreshing<T>(
        info: ItemCacheStateTransitionHandler<T>,
        val dataInDb: CachedValue.Loaded<T>?,
        val downloadState: CachedValue.NotLoaded = CachedValue.Loading,
    ) : ItemCacheState<T>(info) {
        override val data get() = dataInDb ?: downloadState

        override suspend fun next(): ItemCacheState<T> {
            // TODO: better retry strategy
            if (downloadState is CachedValue.Error) {
                delay(downloadState.downloadTime + 5.seconds - Clock.System.now())
            }

            val now = Clock.System.now()
            val ret = info.download()
            return if (ret.isSuccess) {
                Saving(info, CachedValue.Loaded(ret.getOrThrow(), now))
            } else {
                Refreshing(info, dataInDb, CachedValue.Error(ret.exceptionOrNull()!!, now))
            }
        }
    }
}



