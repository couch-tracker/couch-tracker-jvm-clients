package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.github.couchtracker.jvmclients.common.CouchTrackerStyle
import com.github.couchtracker.jvmclients.common.utils.progress
import kotlinx.coroutines.channels.Channel

@Composable
fun <T : AppDestination> StackNavigation(
    stack: StackData<T>,
    dismiss: (T) -> Boolean,
    dataProvider: (T, w: Dp, h: Dp) -> AppDestinationData = { _, _, _ ->
        AppDestinationData()
    },
    modifier: Modifier = Modifier,
    composable: @Composable (BoxScope.(T, AppDestinationData, manualAnimation: ManualAnimation) -> Unit),
) {
    val channel = remember { Channel<List<T>>(Channel.CONFLATED) }
    SideEffect { channel.trySend(stack.stack) }
    val animationSpec = tween<Float>(CouchTrackerStyle.animationDuration.inWholeMilliseconds.toInt())
    var itemsAnimationStates: List<ItemAnimationState<T>> by remember {
        mutableStateOf(stack.stack.map { it.still() })
    }
    var manualAnimations by remember { mutableStateOf(emptyMap<T, ManualAnimation>()) }

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
                    }.map { item ->
                        when (item) {
                            !in prevIndex -> item.enter()
                            !in newIndex -> item.exit()
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
                while (state.any { it.animationState.canProgress }) {
                    val playTime = withFrameNanos { it } - startTime
                    val animationProgress = animation.getValueFromNanos(playTime)
                    state = state.mapNotNull {
                        if (it.animationState.canProgress)
                            it.update(animationProgress)
                        else it
                    }
                    itemsAnimationStates = state
                }
            }
        }
    }
    BoxWithConstraints {
        val w = this.maxWidth
        val wPx = with(LocalDensity.current) { w.toPx() }
        val h = this.maxHeight
        val hPx = with(LocalDensity.current) { w.toPx() }
        val destinations = itemsAnimationStates.mapTo(HashSet()) { it.destination }
        if (manualAnimations.keys != destinations) {
            manualAnimations = destinations.associateWith {
                manualAnimations[it] ?: ManualAnimation { state ->
                    if (!state) dismiss(it)
                    else true
                }
            }
        }


        val animationStatesWithManual = itemsAnimationStates.mapIndexed { index, ias ->
            // I assume the topmost item is always in animation state, so the "hidden" destinations
            // are ready to be rendered, and won't be choppy
            val manualAnimation = manualAnimations.getValue(ias.destination)
            if (index == itemsAnimationStates.size - 1 || manualAnimation.isAnimating) {
                ItemAnimationState(
                    ias.destination,
                    ias.animationState + manualAnimation.toAnimationState()
                )
            } else ias
        }
        val visible: Set<T> = itemsAnimationStates
            .visible(w, h, dataProvider)
            .mapTo(HashSet()) { it.destination }
        val visibleBecauseManual = animationStatesWithManual.visible(w, h, dataProvider)
        val hasDroppedSomething = visibleBecauseManual.size != animationStatesWithManual.size

        Box(modifier) {
            if (visibleBecauseManual.isNotEmpty()) {
                val topVisibility = visibleBecauseManual.last().animationState.visibility(wPx, hPx)

                visibleBecauseManual.forEachIndexed { index, (item, animationState) ->
                    val canPop = hasDroppedSomething || index > 0
                    val data = dataProvider(item, w, h)
                    val isLast = index == visibleBecauseManual.size - 1
                    val animation = manualAnimations.getValue(item)
                    val takesPartInMeasurement = item in visible
                    key(item) {
                        var m = Modifier
                            .graphicsLayer {
                                animationState.setup(this, data.opaque, isLast, wPx)
                                if (!isLast && topVisibility > 0) {
                                    // TODO: that's very similar to AnimationState.Entering... Can we merge them?
                                    this.scaleX *= (0.95f..1f).progress(1f - topVisibility)
                                    this.scaleY *= (0.95f..1f).progress(1f - topVisibility)
                                    this.alpha *= (0.6f..1f).progress(1f - topVisibility)
                                    this.transformOrigin = TransformOrigin(0.5f, 1f)
                                }
                            }
                        if (!takesPartInMeasurement) {
                            m = m.matchParentSize()
                        }
                        Box(m) {
                            animation.width = w
                            animation.height = h
                            animation.canPop = canPop
                            composable(item, data, animation)
                        }
                    }
                }
            }
        }
    }
}