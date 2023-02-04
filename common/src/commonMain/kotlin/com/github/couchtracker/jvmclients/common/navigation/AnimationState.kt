package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.ui.graphics.GraphicsLayerScope
import com.github.couchtracker.jvmclients.common.utils.progress


sealed class AnimationState {
    data class Entering(
        val progress: Float = 0f,
    ) : AnimationState() {

        override fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean) {
            // TODO: better values
            if (isTop && isOpaque) {
                g.translationX = (1 - progress) * 200
            } else {
                g.scaleX = (0.8f..1f).progress(progress)
                g.scaleY = g.scaleX
            }
            g.alpha = progress
        }

        override fun update(progress: Float): AnimationState {
            return if (progress == 1f) Still
            else copy(progress = progress)
        }

        override fun visibility() = progress

    }

    object Still : AnimationState() {
        override fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean) {}
        override fun update(progress: Float) = this
        override fun visibility() = 1f
    }

    data class Exiting(
        val progress: Float = 0f,
    ) :
        AnimationState() {
        override fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean) {
            Entering(1f - progress).setup(g, isOpaque, isTop)
        }

        override fun update(progress: Float): AnimationState? {
            return if (progress == 1f) null
            else copy(progress = progress)
        }

        override fun visibility() = 1 - progress
    }

    abstract fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean)
    abstract fun update(progress: Float): AnimationState?
    abstract fun visibility(): Float
}

data class ItemAnimationState<T : AppDestination<T>>(
    val destination: T,
    val animationState: AnimationState,
) {
    fun opaque(data: AppDestinationData): Boolean = data.opaque && animationState == AnimationState.Still

    fun update(progress: Float): ItemAnimationState<T>? {
        val newAnim = animationState.update(progress)
        return if (newAnim == null) null
        else copy(animationState = newAnim)
    }
}

fun <T : AppDestination<T>> T.still() = ItemAnimationState(this, AnimationState.Still)
fun <T : AppDestination<T>> T.enter() = ItemAnimationState(this, AnimationState.Entering())
fun <T : AppDestination<T>> T.exit() = ItemAnimationState(this, AnimationState.Exiting())

