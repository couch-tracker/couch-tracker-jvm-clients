@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

class ItemAnimatableState(
    zIndex: Int,
    isInitiallyVisible: Boolean = true,
    confirmStateChange: (newValue: Boolean) -> Boolean,
) {
    val zIndex: Animatable<Float, AnimationVector1D> = Animatable(zIndex.toFloat())
    val transitionState: Animatable<Float, AnimationVector1D> = Animatable(if (isInitiallyVisible) 1f else 0f)

    val horizontalSwipe: SwipeableState<Boolean> = SwipeableState(true, confirmStateChange = confirmStateChange)
    val verticalSwipe: SwipeableState<Boolean> = SwipeableState(true, confirmStateChange = confirmStateChange)

    var topItemsVisibility by mutableStateOf(0f)
        internal set
    var width by mutableStateOf(0.dp)
        internal set
    var height by mutableStateOf(0.dp)
        internal set
    var canPop by mutableStateOf(false)
        internal set
    var isOpaque by mutableStateOf(false)
        internal set
    var isOnTop by mutableStateOf(false)
        internal set

    val currentManualOffsetX get() = horizontalSwipe.offset.value
    val currentManualOffsetY get() = verticalSwipe.offset.value

    fun opaque(data: AppDestinationData): Boolean =
        data.opaque && transitionState.value < 1 && horizontalSwipe.offset.value == 0f && verticalSwipe.offset.value == 0f

    fun visibility(width: Float, height: Float): Float {
        return transitionState.value *
                (1 - horizontalSwipe.offset.value.absoluteValue / width) *
                (1 - verticalSwipe.offset.value.absoluteValue / height)
    }
}