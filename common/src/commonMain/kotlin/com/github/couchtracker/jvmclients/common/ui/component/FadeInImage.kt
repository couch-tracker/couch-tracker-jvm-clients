package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.layout.*
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
        if (painter.requestState is ImageRequestState.Success) 1f else 0f,
    )
    Image(
        painter,
        contentDescription,
        modifier.alpha(alpha),
        contentScale = contentScale,
    )
}
