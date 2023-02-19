package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import com.seiko.imageloader.AsyncImagePainter
import com.seiko.imageloader.ImageRequestState

@Composable
fun FadeInImage(
    painter: AsyncImagePainter,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
) {
    val alpha by animateFloatAsState(
        if (painter.requestState is ImageRequestState.Success) 1f else 0f
    )
    Image(
        painter,
        contentDescription,
        modifier.alpha(alpha),
        contentScale = contentScale,
    )
}