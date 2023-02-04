package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.CouchTrackerStyle
import com.github.couchtracker.jvmclients.common.utils.mapIsFirstLast
import kotlinx.coroutines.channels.Channel

@Composable
fun <T : AppDestination<T>> StackNavigation(
    stack: StackData<T>,
    composable: @Composable BoxScope.(T) -> Unit,
) {
    val channel = remember { Channel<List<T>>(Channel.CONFLATED) }
    SideEffect { channel.trySend(stack.stack) }
    val animationSpec = tween<Float>(CouchTrackerStyle.animationDuration.inWholeMilliseconds.toInt())

    var itemsAnimationStates: List<ItemAnimationState<T>> by remember {
        mutableStateOf(stack.stack.map { it.still() })
    }
    LaunchedEffect(channel) {
        var prev = stack.stack
        for (target in channel) {
            val newTarget = channel.tryReceive().getOrNull() ?: target
            val prevTarget = prev
            prev = newTarget
            if (prevTarget != newTarget) {
                val prevIndex: Map<T, Int> = prevTarget.withIndex().associate { it.value to it.index }
                val newIndex: Map<T, Int> = newTarget.withIndex().associate { it.value to it.index }
                var state = (prevIndex.keys + newIndex.keys)
                    .sortedBy {
                        maxOf(
                            prevIndex[it]?.plus(.5f) ?: -1f,
                            newIndex[it]?.plus(.0f) ?: -1f,
                        )
                    }.mapIsFirstLast { item, isFirst, isLast ->
                        val slide = isLast && item.opaque
                        val scale = !slide
                        when (item) {
                            !in prevIndex -> item.enter(slide, scale)
                            !in newIndex -> item.exit(slide, scale)
                            else -> item.still()
                        }
                    }
                val animation = TargetBasedAnimation(
                    animationSpec = animationSpec,
                    typeConverter = Float.VectorConverter,
                    initialValue = 0f,
                    targetValue = 1f
                )
                val startTime = withFrameNanos { it }
                while (state.any { it.animationState != AnimationState.Still }) {
                    val playTime = withFrameNanos { it } - startTime
                    val animationProgress = animation.getValueFromNanos(playTime)
                    state = state.mapNotNull { it.update(animationProgress) }
                    itemsAnimationStates = state
                }
            }
        }
    }
    val visible = itemsAnimationStates.drop(
        itemsAnimationStates.indexOfLast { it.opaque }.coerceAtLeast(0)
    )

    RenderVisibleItems(visible, composable)
}

@Composable
private fun <T : AppDestination<T>> RenderVisibleItems(
    items: List<ItemAnimationState<T>>,
    composable: @Composable BoxScope.(T) -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = CouchTrackerStyle.colors.background) {
        if (items.isNotEmpty()) {
            val topVisibility = items.last().animationState.visibility()

            items.forEachIndexed { index, (item, animationState) ->
                val shouldBlur = index < items.size - 1
                key(item) {
                    var m = Modifier.fillMaxSize()
                        .graphicsLayer {
                            animationState.setup(this)
                            if (shouldBlur) {
                                val blur = (16.dp * topVisibility).toPx()
                                this.renderEffect = BlurEffect(blur, blur, TileMode.Clamp)
                                this.clip = true
                            }
                        }
                    if (shouldBlur) {
                        m = m.drawWithContent {
                            drawContent()
                            drawRect(Color.Black.copy(alpha = topVisibility * 0.2f))
                        }
                    }
                    if (item.opaque) {
                        m = m.background(MaterialTheme.colors.background)
                    }
                    Box(m, contentAlignment = Alignment.Center) {
                        composable(item)
                    }
                }
            }
        }
    }
}