package com.github.couchtracker.jvmclients.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds

object CouchTrackerStyle {
    val animationDuration = 300.milliseconds
    val colors = darkColors()
    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(16.dp),
    )
}