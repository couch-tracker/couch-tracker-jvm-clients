@file:OptIn(ExperimentalCoroutinesApi::class)

package com.github.couchtracker.jvmclients.common.data

import com.github.couchtracker.jvmclients.common.utils.elapsedTime
import mu.KotlinLogging
import java.util.Locale
import java.util.Optional
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed class CachedValue<out T> {
    sealed class NotLoaded : CachedValue<Nothing>()
    object Loading : NotLoaded()
    data class Error(val error: DisplayableError) : NotLoaded()
    data class Loaded<T>(val data: T, val downloadTime: Instant) : CachedValue<T>() {
        val expireDate = downloadTime.plus(1.days)
        val isUpToDate = Clock.System.now().minus(1.days) < expireDate
    }
}

fun <T> Array<CachedValue<T>>.combine(): CachedValue<List<T>> {
    if (isEmpty()) return CachedValue.Loaded(emptyList(), Clock.System.now())

    if (all { it is CachedValue.Loaded }) {
        return CachedValue.Loaded<List<T>>(
            map { (it as CachedValue.Loaded).data },
            minOf { (it as CachedValue.Loaded).downloadTime },
        )
    }

    if (all { it is CachedValue.Loading }) {
        return CachedValue.Loading
    }

    return CachedValue.Error(
        mapNotNull { (it as? CachedValue.Error)?.error }.merge(),
    )
}

private val logger = KotlinLogging.logger {}

data class LocalizedKey<K>(val locales: List<Locale>, val key: K)

class ItemsCache<K, T> private constructor(
    private val scope: CoroutineScope,
    private val load: suspend (key: K) -> Optional<CachedValue.Loaded<T>>,
    private val save: suspend (key: K, CachedValue.Loaded<T>) -> Unit,
    private val download: suspend (key: K) -> Result<T>,
    private val reauthenticate: () -> Unit,
) {
    private val cache = mutableMapOf<K, ItemCache>()
    fun get(key: K): Flow<CachedValue<T>> {
        return cache.getOrPut(key) {
            getItem(key)
        }.flow
    }

    private fun getItem(key: K): ItemCache {
        return ItemCache(key)
    }

    fun retryAuthErrors() {
        cache.values.forEach {
            it.retry()
        }
    }

    private inner class ItemCache(val key: K) {
        val info = ItemCacheStateTransitionHandler(
            reauthenticate,
            { load(key) },
            { save(key, it) },
            { download(key) },
        )
        val startState = MutableStateFlow<ItemCacheState<T>>(ItemCacheState.Unknown(info))
        val flow = startState.transformLatest { initialState ->
            var state: ItemCacheState<T> = initialState
            emit(state.data)
            while (true) {
                state = state.next {
                    retry()
                }
                emit(state.data)
            }
        }
            .flowOn(Dispatchers.IO)
            .shareIn(scope, SharingStarted.WhileSubscribed(), 1)

        fun retry(force: Boolean = false) {
            val last = flow.replayCache.lastOrNull()
            if (last is CachedValue.Error) {
                startState.value = ItemCacheState.Refreshing(info, last)
            } else if (last == null || force) {
                startState.value = ItemCacheState.Unknown(info)
            }
        }
    }

    companion object {
        fun <K, T> persistent(
            scope: CoroutineScope,
            load: suspend (key: K) -> Optional<CachedValue.Loaded<T>>,
            save: suspend (key: K, CachedValue.Loaded<T>) -> Unit,
            download: suspend (key: K) -> Result<T>,
            reauthenticate: () -> Unit,
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
                reauthenticate,
            )
        }

        fun <K, T> volatile(
            scope: CoroutineScope,
            download: suspend (key: K) -> Result<T>,
            reauthenticate: () -> Unit,
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
                reauthenticate,
            )
        }
    }
}

private class ItemCacheStateTransitionHandler<T>(
    val reauthenticate: () -> Unit,
    val load: suspend () -> Optional<CachedValue.Loaded<T>>,
    val save: suspend (CachedValue.Loaded<T>) -> Unit,
    val download: suspend () -> Result<T>,
)

private sealed class ItemCacheState<T>(val info: ItemCacheStateTransitionHandler<T>) {
    abstract val data: CachedValue<T>

    abstract suspend fun next(retry: () -> Unit): ItemCacheState<T>

    class Unknown<T>(info: ItemCacheStateTransitionHandler<T>) : ItemCacheState<T>(info) {
        override val data get() = CachedValue.Loading

        override suspend fun next(retry: () -> Unit): ItemCacheState<T> {
            val data = info.load()
            return if (data.isPresent) {
                if (data.get().isUpToDate) {
                    Updated(info, data.get())
                } else {
                    Refreshing(info, data.get())
                }
            } else {
                Refreshing(info, CachedValue.Loading)
            }
        }
    }

    class Updated<T>(info: ItemCacheStateTransitionHandler<T>, override val data: CachedValue.Loaded<T>) : ItemCacheState<T>(info) {
        override suspend fun next(retry: () -> Unit): Refreshing<T> {
            delay(data.expireDate - Clock.System.now())
            return Refreshing(info, data)
        }
    }

    class Saving<T>(info: ItemCacheStateTransitionHandler<T>, override val data: CachedValue.Loaded<T>) : ItemCacheState<T>(info) {
        override suspend fun next(retry: () -> Unit): Updated<T> {
            info.save(data)
            return Updated(info, data)
        }
    }

    class WaitingForRefresh<T>(
        info: ItemCacheStateTransitionHandler<T>,
        val fallbackData: CachedValue<T>,
        val failureTime: Instant,
        val consecutiveFailures: Int = 0,
    ) : ItemCacheState<T>(info) {
        override val data: CachedValue<T> get() = fallbackData

        override suspend fun next(retry: () -> Unit): ItemCacheState<T> {
            val delay = 2f.pow(consecutiveFailures).coerceAtMost(120f).roundToLong().seconds
            delay(failureTime + delay - Clock.System.now())
            return Refreshing(info, data, consecutiveFailures)
        }
    }

    class PermanentError<T>(
        info: ItemCacheStateTransitionHandler<T>,
        val fallbackData: CachedValue<T>,
    ) : ItemCacheState<T>(info) {
        override val data: CachedValue<T> get() = fallbackData

        override suspend fun next(retry: () -> Unit): ItemCacheState<T> {
            delay(Long.MAX_VALUE)
            error("Unreachable code")
        }
    }

    class Refreshing<T>(
        info: ItemCacheStateTransitionHandler<T>,
        val fallbackData: CachedValue<T>,
        val consecutiveFailures: Int = 0,
    ) : ItemCacheState<T>(info) {
        override val data: CachedValue<T> get() = fallbackData

        override suspend fun next(retry: () -> Unit): ItemCacheState<T> {
            val now = Clock.System.now()
            val ret = info.download()
            return if (ret.isSuccess) {
                Saving(info, CachedValue.Loaded(ret.getOrThrow(), now))
            } else {
                val error = DisplayableError.fromException(ret.exceptionOrNull()!!, retry, info.reauthenticate)
                val newData = when (data) {
                    is CachedValue.Loaded -> data
                    else -> CachedValue.Error(error)
                }
                if (error.retryable) {
                    WaitingForRefresh(info, newData, now, consecutiveFailures + 1)
                } else {
                    PermanentError(info, newData)
                }
            }
        }
    }
}
