package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import com.github.couchtracker.jvmclients.common.utils.progress


sealed class AnimationState {
    data class Entering(
        val progress: Float = 0f,
    ) : AnimationState() {

        override fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean, width: Float) {
            if (isTop && isOpaque) {
                g.translationX += (1 - progress) * width
            } else {
                g.scaleX *= (0.95f..1f).progress(progress)
                g.scaleY *= (0.95f..1f).progress(progress)
                g.transformOrigin = TransformOrigin(0.5f, 1f)
                g.alpha *= progress
            }
        }

        override val canProgress = true
        override fun update(progress: Float): AnimationState {
            return if (progress == 1f) Still
            else copy(progress = progress)
        }

        override fun visibility(width: Float) = progress

    }

    object Still : AnimationState() {
        override fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean, width: Float) {}
        override fun update(progress: Float) = this
        override fun visibility(width: Float) = 1f
    }

    data class Exiting(
        val progress: Float = 0f,
    ) : AnimationState() {

        override fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean, width: Float) {
            Entering(1f - progress).setup(g, isOpaque, isTop, width)
        }

        override val canProgress = true
        override fun update(progress: Float): AnimationState? {
            return if (progress == 1f) null
            else copy(progress = progress)
        }

        override fun visibility(width: Float) = 1 - progress
    }

    data class ManuallyExiting(
        val translate: Float,
    ) : AnimationState() {

        override fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean, width: Float) {
            g.translationX += translate
        }

        override fun update(progress: Float): AnimationState? {
            throw IllegalStateException()
        }

        override fun visibility(width: Float) = 1 - (translate / width).coerceIn(0f, 1f)
    }

    data class Combined(
        val a: AnimationState,
        val b: AnimationState,
    ) : AnimationState() {
        override fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean, width: Float) {
            a.setup(g, isOpaque, isTop, width)
            b.setup(g, isOpaque, isTop, width)
        }

        override val canProgress: Boolean
            get() = a.canProgress || b.canProgress

        override fun update(progress: Float): AnimationState? {
            val na = if (a.canProgress) a.update(progress) else a
            val nb = if (b.canProgress) b.update(progress) else b
            return if (na == null) nb
            else if (nb == null) na
            else na + nb
        }

        override fun visibility(width: Float): Float {
            return a.visibility(width) * b.visibility(width)
        }
    }

    abstract fun setup(g: GraphicsLayerScope, isOpaque: Boolean, isTop: Boolean, width: Float)
    open val canProgress: Boolean = false
    abstract fun update(progress: Float): AnimationState?
    abstract fun visibility(width: Float): Float

    operator fun plus(another: AnimationState): AnimationState {
        return if (another is Still) this
        else if (this is Still) another
        else Combined(this, another)
    }
}

data class ItemAnimationState<T : AppDestination>(
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

fun <T : AppDestination> T.still() = ItemAnimationState(this, AnimationState.Still)
fun <T : AppDestination> T.enter() = ItemAnimationState(this, AnimationState.Entering())
fun <T : AppDestination> T.exit() = ItemAnimationState(this, AnimationState.Exiting())

