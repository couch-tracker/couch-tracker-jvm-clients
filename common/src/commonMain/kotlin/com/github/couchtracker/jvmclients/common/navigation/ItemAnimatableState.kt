package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

class ItemAnimatableState(
    zIndex: Int,
    isInitiallyVisible: Boolean = true,
    confirmStateChange: (newValue: Boolean) -> Boolean,
) {
    val manualAnimation = ManualAnimation(confirmStateChange)
    val zIndex: Animatable<Float, AnimationVector1D> = Animatable(zIndex.toFloat())
    val transitionState: Animatable<Float, AnimationVector1D> = Animatable(if (isInitiallyVisible) 1f else 0f)

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

    fun opaque(data: AppDestinationData): Boolean =
        data.opaque && transitionState.value < 1 && !manualAnimation.isAnimating

    fun visibility(width: Float, height: Float): Float {
        return transitionState.value * manualAnimation.visibility(width, height)
    }
}