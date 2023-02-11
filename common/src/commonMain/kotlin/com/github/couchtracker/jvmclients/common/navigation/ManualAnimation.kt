@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

class ManualAnimation(
    confirmStateChange: (newValue: Boolean) -> Boolean = { true },
) {
    var width by mutableStateOf(0.dp)
        internal set
    var height by mutableStateOf(0.dp)
        internal set
    var canPop by mutableStateOf(false)
        internal set

    val horizontalSwipe: SwipeableState<Boolean> = SwipeableState(true, confirmStateChange = confirmStateChange)
    val verticalSwipe: SwipeableState<Boolean> = SwipeableState(true, confirmStateChange = confirmStateChange)

    val isAnimating: Boolean get() = horizontalSwipe.offset.value != 0f && verticalSwipe.offset.value != 0f

    fun toAnimationState() = AnimationState.ManuallyExiting(
        translateX = horizontalSwipe.offset.value,
        translateY = verticalSwipe.offset.value,
    )
}