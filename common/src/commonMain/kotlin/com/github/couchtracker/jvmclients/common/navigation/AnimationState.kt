package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.ui.graphics.GraphicsLayerScope
import com.github.couchtracker.jvmclients.common.utils.progress


sealed class AnimationState {
    data class Entering(
        val progress: Float = 0f,
        val slide: Boolean = false,
        val scale: Boolean = false,
        val alpha: Boolean = true,
    ) : AnimationState() {

        override fun setup(g: GraphicsLayerScope) {
            // TODO: better values
            if (scale) {
                g.scaleX = (0.7f..1f).progress(progress)
                g.scaleY = g.scaleX
            }
            if (slide) {
                g.translationX = (1 - progress) * 200
            }
            if (alpha) {
                g.alpha = progress
            }
        }

        override fun update(progress: Float): AnimationState {
            return if (progress == 1f) Still
            else copy(progress = progress)
        }

        override fun visibility() = progress

    }

    object Still : AnimationState() {
        override fun setup(g: GraphicsLayerScope) {}
        override fun update(progress: Float) = this
        override fun visibility() = 1f
    }

    data class Exiting(
        val progress: Float = 0f,
        val slide: Boolean = false,
        val scale: Boolean = false,
        val alpha: Boolean = true,
    ) :
        AnimationState() {
        override fun setup(g: GraphicsLayerScope) {
            Entering(1f - progress, slide, scale, alpha).setup(g)
        }

        override fun update(progress: Float): AnimationState? {
            return if (progress == 1f) null
            else copy(progress = progress)
        }

        override fun visibility() = 1 - progress
    }

    abstract fun setup(g: GraphicsLayerScope)
    abstract fun update(progress: Float): AnimationState?
    abstract fun visibility(): Float
}

data class ItemAnimationState<T : AppDestination<T>>(
    val stackItem: StackItem<T>,
    val animationState: AnimationState,
) {
    val opaque = stackItem.destination.opaque && animationState == AnimationState.Still
    fun update(progress: Float): ItemAnimationState<T>? {
        val newAnim = animationState.update(progress)
        return if (newAnim == null) null
        else copy(animationState = newAnim)
    }
}

fun <T : AppDestination<T>> StackItem<T>.still() = ItemAnimationState(this, AnimationState.Still)
fun <T : AppDestination<T>> StackItem<T>.enter(
    slide: Boolean = false,
    scale: Boolean = false,
    alpha: Boolean = true,
) = ItemAnimationState(this, AnimationState.Entering(0f, slide, scale, alpha))

fun <T : AppDestination<T>> StackItem<T>.exit(
    slide: Boolean = false,
    scale: Boolean = false,
    alpha: Boolean = true,
) = ItemAnimationState(this, AnimationState.Exiting(0f, slide, scale, alpha))

