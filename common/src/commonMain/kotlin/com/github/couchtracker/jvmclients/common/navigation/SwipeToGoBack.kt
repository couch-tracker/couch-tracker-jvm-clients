@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.foundation.gestures.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun Modifier.swipeToGoBack(
    offsetX: SwipeableState<Boolean>,
): Modifier {
    var w by remember { mutableStateOf(128) }

    return onSizeChanged { w = it.width }
        .swipeable(
            offsetX,
            anchors = mapOf(0f to true, w.toFloat() to false),
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Horizontal,
        )
}