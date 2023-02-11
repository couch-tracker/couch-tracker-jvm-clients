@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.foundation.gestures.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

@Composable
fun Modifier.swipeToGoBack(
    manualAnimation: ManualAnimation,
    vertical: Boolean = false,
    horizontal: Boolean = true,
): Modifier {
    if (!vertical && !horizontal) return this

    var ret = this
    if (horizontal) {
        val canSwipeBack = manualAnimation.canPop

        val happyPathAnchors = mapOf(0f to true, with(LocalDensity.current) { manualAnimation.width.toPx() } to false)
        val anchors = if (canSwipeBack) happyPathAnchors else mapOf(0f to true)
        val resistance = if (canSwipeBack) SwipeableDefaults.StandardResistanceFactor
        else SwipeableDefaults.StiffResistanceFactor
        ret = ret.swipeable(
            manualAnimation.horizontalSwipe,
            anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Horizontal,
            resistance = SwipeableDefaults.resistanceConfig(happyPathAnchors.keys, resistance, resistance),
        )
    }
    if (vertical) {
        val anchors = mapOf(0f to true, with(LocalDensity.current) { manualAnimation.height.toPx() } to false)
        ret = ret.swipeable(
            manualAnimation.verticalSwipe,
            anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Vertical,
            resistance = SwipeableDefaults.resistanceConfig(anchors.keys, 0f),
        )
    }
    return ret
}