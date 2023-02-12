package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@Composable
fun <T : AppDestination> StackNavigation(
    stack: StackData<T>,
    dismiss: (T) -> Boolean,
    dataProvider: (T, w: Dp, h: Dp) -> AppDestinationData = { _, _, _ ->
        AppDestinationData()
    },
    modifier: Modifier = Modifier,
    composable: @Composable (BoxScope.(T, state: ItemAnimatableState) -> Unit),
) {
    val channel = remember { Channel<List<T>>(Channel.CONFLATED) }
    SideEffect { channel.trySend(stack.stack) }
    var itemStates: Map<T, ItemAnimatableState> by remember {
        mutableStateOf(stack.stack.withIndex().associate { (index, it) ->
            it to ItemAnimatableState(index) { state ->
                if (!state) dismiss(it)
                else true
            }
        })
    }

    LaunchedEffect(channel) {
        var prev = stack.stack
        for (target in channel) {
            val newTarget = channel.tryReceive().getOrNull() ?: target
            val prevTarget = prev
            prev = newTarget
            if (prevTarget != newTarget) {
                val newIndex: Map<T, Int> = newTarget.withIndex().associate { it.value to it.index }

                val thingsGoingOn = mutableListOf<Job>()

                prevTarget.forEach { pt ->
                    val state = itemStates.getValue(pt)
                    if (pt !in newIndex) {
                        thingsGoingOn += launch {
                            state.transitionState.animateTo(0f)
                            itemStates = itemStates.minus(pt)
                        }
                    }
                }
                newTarget.forEach { nt ->
                    val ni = newIndex.getValue(nt)
                    val state = itemStates[nt] ?: ItemAnimatableState(ni, false) { state ->
                        if (!state) dismiss(nt)
                        else true
                    }
                    if (nt !in itemStates) {
                        itemStates = itemStates.plus(nt to state)
                    }
                    if (state.zIndex.targetValue != ni.toFloat()) {
                        thingsGoingOn += launch {
                            state.zIndex.animateTo(ni.toFloat())
                        }
                    }
                    if (state.transitionState.targetValue != 1f) {
                        thingsGoingOn += launch {
                            state.transitionState.animateTo(1f)
                        }
                    }
                }

                thingsGoingOn.joinAll()
            }
        }
    }
    BoxWithConstraints {
        val w = this.maxWidth
        val wPx = with(LocalDensity.current) { w.toPx() }
        val h = this.maxHeight
        val hPx = with(LocalDensity.current) { h.toPx() }
        val sortedStates = itemStates
            .entries
            .sortedBy { it.value.zIndex.value }


        val visible = sortedStates.visible(w, h, dataProvider)
        val hasDroppedSomething = visible.size != itemStates.size

        Box(modifier) {
            if (visible.isNotEmpty()) {
                val topMostVisibility = visible.last().value.visibility(wPx, hPx)
                visible.forEachIndexed { index, (destination, state) ->
                    val canPop = hasDroppedSomething || index > 0
                    val data = dataProvider(destination, w, h)
                    val isTopOfStack = index == visible.size - 1

                    key(destination) {
                        Box(Modifier) {
                            state.width = w
                            state.height = h
                            state.canPop = canPop
                            state.isOpaque = data.opaque
                            state.isOnTop = isTopOfStack
                            // TODO: compute visibility of all items on top, and not just the topmost
                            state.topItemsVisibility = if (isTopOfStack) 0f else topMostVisibility
                            composable(destination, state)
                        }
                    }
                }
            }
        }
    }
}