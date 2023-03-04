package com.github.couchtracker.jvmclients.common.utils

import androidx.compose.ui.graphics.*

fun Color.blend(another: Color, progress: Float): Color {
    return Color(
        red = (red..another.red).progress(progress),
        green = (green..another.green).progress(progress),
        blue = (blue..another.blue).progress(progress),
        alpha = (alpha..another.alpha).progress(progress),
    )
}
