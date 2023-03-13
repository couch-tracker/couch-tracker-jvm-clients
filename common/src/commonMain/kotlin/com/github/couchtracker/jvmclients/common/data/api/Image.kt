package com.github.couchtracker.jvmclients.common.data.api

import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import mu.KotlinLogging
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

private val logger = KotlinLogging.logger {}

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
        val out = if (width != null && height != null) {
            sourceForInternal(maxOf(width, (height * biggerSource.aspectRatio).roundToInt()))
        } else if (width != null) {
            sourceForInternal(width)
        } else if (height != null) {
            sourceForInternal((height * biggerSource.aspectRatio).roundToInt())
        } else {
            // When I have no  information about the desired size, I use a full-HDish image
            sourceFor(1920, 1920)
        }
        logger.debug { "Returning ${out.width}x${out.height} for width=$width; height=$height" }
        return out
    }

    private fun sourceForInternal(width: Int): ImageSource {
        // Images that are slightly bigger than we need look better
        val additionalScaling = 1.25f
        val targetW = additionalScaling * width
        return sources.minBy {
            // Prefer a bigger, rather than a smaller image
            val lowResPenalty = if (targetW > it.width) 10 else 1
            lowResPenalty * (it.width - targetW) * (it.width - targetW)
        }
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
