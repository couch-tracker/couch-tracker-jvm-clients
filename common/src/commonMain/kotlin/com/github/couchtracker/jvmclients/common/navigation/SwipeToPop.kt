@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.foundation.gestures.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*

@Composable
fun Modifier.swipeToPop(
    state: ItemAnimatableState,
    screenWidth: Dp,
    screenHeight: Dp,
    vertical: Boolean = true,
    horizontal: Boolean = false,
): Modifier {
    var ret = this
    if (horizontal) {
        ret = ret.createSwipeable(state.canPop, screenWidth, state.horizontalSwipe, Orientation.Horizontal)
    }
    if (vertical) {
        ret = ret.createSwipeable(state.canPop, screenHeight, state.verticalSwipe, Orientation.Vertical, true)
    }
    return ret
}

@Composable
fun Modifier.createSwipeable(
    canPop: Boolean,
    size: Dp,
    swipeableState: SwipeableState<Boolean>,
    orientation: Orientation,
    blockWrongDirectionSwiper: Boolean = false,
): Modifier {
    val happyPathAnchors = mapOf(0f to true, with(LocalDensity.current) { size.toPx() } to false)
    val anchors = if (canPop) happyPathAnchors else mapOf(0f to true)
    val resistance = if (canPop) {
        SwipeableDefaults.StandardResistanceFactor
    } else {
        SwipeableDefaults.StiffResistanceFactor
    }

    return swipeable(
        swipeableState,
        anchors = anchors,
        thresholds = { _, _ -> FractionalThreshold(0.5f) },
        orientation = orientation,
        resistance = SwipeableDefaults.resistanceConfig(
            happyPathAnchors.keys,
            if (blockWrongDirectionSwiper) 0f else resistance,
            resistance,
        ),
    )
}
