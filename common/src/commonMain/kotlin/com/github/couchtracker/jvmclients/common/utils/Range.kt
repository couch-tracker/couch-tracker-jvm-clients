package com.github.couchtracker.jvmclients.common.utils

import androidx.compose.ui.unit.Dp

typealias FloatRange = ClosedFloatingPointRange<Float>

fun FloatRange.progress(progress: Float) = start + progress * (endInclusive - start)

fun ClosedRange<Dp>.progress(progress: Float): Dp = start + (endInclusive - start) * progress