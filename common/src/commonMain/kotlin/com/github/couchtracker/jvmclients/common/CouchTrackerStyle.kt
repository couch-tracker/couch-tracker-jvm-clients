package com.github.couchtracker.jvmclients.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.utils.blend
import kotlin.time.Duration.Companion.milliseconds

object CouchTrackerStyle {
    val darkColors = darkColors().let { colors ->
        colors.copy(
            surface = colors.primary.blend(colors.background, 0.9f),
            background = colors.primary.blend(colors.background, 0.8f),
        )
    }
    val lightColors = lightColors()
    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(16.dp),
    )
}