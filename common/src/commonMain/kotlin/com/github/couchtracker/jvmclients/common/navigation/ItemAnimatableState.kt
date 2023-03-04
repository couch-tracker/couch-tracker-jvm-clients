@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.animation.core.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ItemAnimatableState(
    zIndex: Int,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    isInitiallyVisible: Boolean = true,
    confirmStateChange: (newValue: Boolean) -> Boolean,
) {
    val zIndex: Animatable<Float, AnimationVector1D> = Animatable(zIndex.toFloat())
    val transitionState: Animatable<Float, AnimationVector1D> = Animatable(if (isInitiallyVisible) 1f else 0f)

    val horizontalSwipe: SwipeableState<Boolean> = SwipeableState(true, animationSpec, confirmStateChange)
    val horizontalSwipeProgress
        get(): Float {
            val progress = horizontalSwipe.progress
            return when {
                progress.to -> 1 - progress.fraction
                else -> progress.fraction
            }
        }
    val verticalSwipe: SwipeableState<Boolean> = SwipeableState(true, animationSpec, confirmStateChange)
    val verticalSwipeProgress
        get(): Float {
            val progress = verticalSwipe.progress
            return when {
                progress.to -> 1 - progress.fraction
                else -> progress.fraction
            }
        }

    /** This is a lambda because computation is expensive */
    var topItemsVisibility by mutableStateOf({ 0f })
        internal set
    var itemsOnTopCanSeeBehind by mutableStateOf(false)
        internal set
    var canPop by mutableStateOf(false)
        internal set
    var isOpaque by mutableStateOf(false)
        internal set
    var isOnTop by mutableStateOf(false)
        internal set
    val isAnimating by derivedStateOf {
        transitionState.value < 1 || horizontalSwipe.offset.value != 0f || verticalSwipe.offset.value != 0f
    }

    val currentManualOffsetX get() = horizontalSwipe.offset.value
    val currentManualOffsetY get() = verticalSwipe.offset.value

    fun visibility(): Float {
        return transitionState.value *
            (1 - horizontalSwipeProgress) *
            (1 - verticalSwipeProgress)
    }
}
