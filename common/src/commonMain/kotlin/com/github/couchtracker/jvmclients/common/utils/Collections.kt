package com.github.couchtracker.jvmclients.common.utils

inline fun <T, R> List<T>.mapIsFirstLast(transform: (T, isFirst: Boolean, isLast: Boolean) -> R): List<R> {
    return mapIndexed { index, t ->
        transform(t, index == 0, index == size - 1)
    }
}