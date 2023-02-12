@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.foundation.gestures.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

@Composable
fun Modifier.swipeToGoBack(
    state: ItemAnimatableState,
    vertical: Boolean = state.canPop,
    horizontal: Boolean = false,
): Modifier {
    if (!vertical && !horizontal) return this

    var ret = this
    if (horizontal) {
        val canSwipeBack = state.canPop

        val happyPathAnchors = mapOf(0f to true, with(LocalDensity.current) { state.width.toPx() } to false)
        val anchors = if (canSwipeBack) happyPathAnchors else mapOf(0f to true)
        val resistance = if (canSwipeBack) SwipeableDefaults.StandardResistanceFactor
        else SwipeableDefaults.StiffResistanceFactor
        ret = ret.swipeable(
            state.manualAnimation.horizontalSwipe,
            anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Horizontal,
            resistance = SwipeableDefaults.resistanceConfig(happyPathAnchors.keys, resistance, resistance),
        )
    }
    if (vertical) {
        val anchors = mapOf(0f to true, with(LocalDensity.current) { state.height.toPx() } to false)
        ret = ret.swipeable(
            state.manualAnimation.verticalSwipe,
            anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Vertical,
            resistance = SwipeableDefaults.resistanceConfig(anchors.keys, 0f),
        )
    }
    return ret
}