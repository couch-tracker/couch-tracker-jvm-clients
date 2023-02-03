package com.github.couchtracker.jvmclients.common.utils

typealias FloatRange = ClosedFloatingPointRange<Float>

fun FloatRange.progress(progress: Float) = start + progress * (endInclusive - start)