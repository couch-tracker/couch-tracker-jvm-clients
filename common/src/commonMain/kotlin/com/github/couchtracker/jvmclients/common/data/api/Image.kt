package com.github.couchtracker.jvmclients.common.data.api

import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val sources: Set<ImageSource>,
) {
    val biggerSource = sources.maxBy { it.height }

    @Composable
    fun sourceFor(
        width: Dp? = null,
        height: Dp? = null,
    ): ImageSource {
        with(LocalDensity.current) {
            return sourceFor(
                width?.toPx()?.roundToInt(),
                height?.toPx()?.roundToInt(),
            )
        }
    }

    fun sourceFor(
        width: Int? = null,
        height: Int? = null,
    ): ImageSource {
        return if (width != null && height != null) {
            sourceForInternal(maxOf(width, (height * biggerSource.aspectRatio).roundToInt()))
        } else if (width != null) {
            sourceForInternal(width)
        } else if (height != null) {
            sourceForInternal((height * biggerSource.aspectRatio).roundToInt())
        } else {
            // When I have no  information about the desired size, I use a full-HDish image
            sourceFor(1920, 1920)
        }
    }

    private fun sourceForInternal(width: Int): ImageSource {
        return sources.minBy { (it.width - width) * (it.width - width) }
    }
}

@Serializable
data class ImageSource(
    val url: String,
    val width: Int,
    val height: Int,
) {
    val aspectRatio get() = width.toFloat() / height.toFloat()
}
