package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.CouchTrackerStyle
import kotlinx.coroutines.channels.Channel

@Composable
fun <T : AppDestination<T>> StackNavigation(
    stack: StackData<T>,
    dataProvider: (T, w: Dp, h: Dp) -> AppDestinationData,
    modifier: Modifier = Modifier,
    composable: @Composable (BoxScope.(T, AppDestinationData, manualAnimation: Animatable<Float, AnimationVector1D>) -> Unit),
) {
    val channel = remember { Channel<List<T>>(Channel.CONFLATED) }
    SideEffect { channel.trySend(stack.stack) }
    val animationSpec = tween<Float>(CouchTrackerStyle.animationDuration.inWholeMilliseconds.toInt())
    var itemsAnimationStates: List<ItemAnimationState<T>> by remember {
        mutableStateOf(stack.stack.map { it.still() })
    }
    var manualAnimations by remember { mutableStateOf(emptyMap<T, Animatable<Float, AnimationVector1D>>()) }

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
        val destinations = itemsAnimationStates.mapTo(HashSet()) { it.destination }
        if (manualAnimations.keys != destinations) {
            manualAnimations = destinations.associateWith {
                manualAnimations[it] ?: Animatable(0f)
            }
        }


        val animationStatesWithManual = itemsAnimationStates
            .map { ias ->
                val manualOffset = manualAnimations.getValue(ias.destination).value
                if (manualOffset > 0f) {
                    ItemAnimationState(
                        ias.destination,
                        ias.animationState + AnimationState.ManuallyExiting(manualOffset)
                    )
                } else ias
            }
        val visible: Set<T> = itemsAnimationStates
            .visible(w, h, dataProvider)
            .mapTo(HashSet()) { it.destination }
        val visibleBecauseManual = animationStatesWithManual.visible(w, h, dataProvider)

        Surface(color = CouchTrackerStyle.colors.background) {
           Box(modifier) {
               if (visibleBecauseManual.isNotEmpty()) {
                   val topVisibility = visibleBecauseManual.last().animationState.visibility(wPx)

                   visibleBecauseManual.forEachIndexed { index, (item, animationState) ->
                       val data = dataProvider(item, w, h)
                       val isLast = index == visibleBecauseManual.size - 1
                       val animation = manualAnimations.getValue(item)
                       val takesPartInMeasurement = item in visible
                       key(item) {
                           var m = Modifier
                               .graphicsLayer {
                                   animationState.setup(this, data.opaque, isLast, wPx)
                                   if (!isLast && topVisibility > 0) {
                                       val blur = (16.dp * topVisibility).toPx()
                                       this.renderEffect = BlurEffect(blur, blur, TileMode.Clamp)
                                       this.clip = true
                                   }
                               }
                           if (!isLast) {
                               m = m.drawWithContent {
                                   drawContent()
                                   drawRect(Color.Black.copy(alpha = topVisibility * 0.2f))
                               }
                           }
                           if (data.opaque) {
                               m = m.background(MaterialTheme.colors.background)
                           }
                           if(!takesPartInMeasurement){
                               m = m.matchParentSize()
                           }
                           Box(m) {
                               composable(item, data, animation)
                           }
                       }
                   }
               }
           }
        }
    }
}