package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import com.github.couchtracker.jvmclients.common.utils.progress

fun Modifier.stackAnimation(
    state: ItemAnimatableState,
    preferVertical: Boolean = true,
): Modifier {
    return graphicsLayer {
        val appearanceProgress = state.transitionState.value
        val focusProgress = 1 - state.topItemsVisibility
        val visibilityProgress = appearanceProgress * focusProgress

        val translateVertically = when {
            state.manualAnimation.currentOffsetY != 0f -> true
            state.manualAnimation.currentOffsetX != 0f -> false
            else -> preferVertical
        }
        val ty = if (translateVertically) (1 - appearanceProgress) * state.height.toPx() else 0f
        val tx = if (!translateVertically) (1 - appearanceProgress) * state.width.toPx() else 0f
        this.translationY = ty + state.manualAnimation.currentOffsetY
        this.translationX = tx + state.manualAnimation.currentOffsetX
        this.scaleX *= (0.95f..1f).progress(visibilityProgress)
        this.scaleY *= (0.95f..1f).progress(visibilityProgress)
        this.transformOrigin = TransformOrigin(0.5f, 1f)
        this.alpha = (0.6f..1f).progress(focusProgress)
    }
}

fun Modifier.slideAnimation(state: ItemAnimatableState): Modifier {
    return graphicsLayer {
        val appearanceProgress = state.transitionState.value * (1 - state.topItemsVisibility)
        val direction  = if(state.isOnTop) 1f else -1f
        this.translationX = direction * (1 - appearanceProgress) * state.width.toPx() + state.manualAnimation.currentOffsetX
    }
}