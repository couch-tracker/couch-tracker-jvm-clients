package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.ui.unit.Dp


interface AppDestination

data class AppDestinationData(
    val opaque: Boolean = true,
)

data class StackData<T : AppDestination>(
    val stack: List<T> = emptyList(),
) {
    init {
        require(stack.isNotEmpty())
    }

    fun pop() = StackData(stack.dropLast(1))
    fun pop(item: T): StackData<T> {
        return StackData(stack.minus(item))
    }

    fun push(item: T): StackData<T> {
        return StackData(stack.minus(item).plus(item))
    }

    fun popTo(toPop: T, toPush: T): StackData<T> {
        return pop(toPop).push(toPush)
    }

    fun canPop(): Boolean {
        return stack.size > 1
    }

    fun contains(item: T) = stack.contains(item)

    companion object {
        fun <T : AppDestination> of(home: T) = StackData(listOf(home))
    }
}

fun <T : AppDestination> List<ItemAnimationState<T>>.visible(
    w: Dp, h: Dp,
    dataProvider: (T, w: Dp, h: Dp) -> AppDestinationData,
): List<ItemAnimationState<T>> {
    return drop(
        withIndex()
            .indexOfLast { (index, element) ->
                element.opaque(dataProvider(element.destination, w, h))
            }.coerceAtLeast(0)
    )
}