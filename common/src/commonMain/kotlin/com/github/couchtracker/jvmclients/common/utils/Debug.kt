package com.github.couchtracker.jvmclients.common.utils

import mu.KLogger
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
inline fun <T> KLogger.elapsedTime(tag: String, f: () -> T): T {
    val ret = measureTimedValue(f)
    debug { "$tag took ${ret.duration}" }
    return ret.value
}
