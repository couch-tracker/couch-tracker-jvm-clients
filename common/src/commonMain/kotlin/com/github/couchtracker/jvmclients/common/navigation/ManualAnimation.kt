@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

class ManualAnimation(
    confirmStateChange: (newValue: Boolean) -> Boolean,
) {

    val horizontalSwipe: SwipeableState<Boolean> = SwipeableState(true, confirmStateChange = confirmStateChange)
    val verticalSwipe: SwipeableState<Boolean> = SwipeableState(true, confirmStateChange = confirmStateChange)

    val currentOffsetX get() = horizontalSwipe.offset.value
    val currentOffsetY get() = verticalSwipe.offset.value

    val isAnimating: Boolean get() = horizontalSwipe.offset.value != 0f && verticalSwipe.offset.value != 0f

    fun visibility(width: Float, height: Float): Float {
        return (1 - horizontalSwipe.offset.value.absoluteValue / width) *
                (1 - verticalSwipe.offset.value.absoluteValue / height)
    }
}