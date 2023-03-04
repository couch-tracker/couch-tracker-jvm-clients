package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.runtime.*

interface AppDestination

data class StackData<T : AppDestination>(
    val stack: List<T> = emptyList(),
) {
    init {
        require(stack.isNotEmpty())
    }

    fun pop() = StackData(stack.dropLast(1))
    fun popOrNull() = if (canPop()) pop() else null
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

@Composable
fun <T : AppDestination> ((StackData<T>?) -> Unit).popOrNull(stackData: StackData<T>): () -> Unit {
    return {
        this(stackData.popOrNull())
    }
}

fun <T : AppDestination> List<Pair<T, ItemAnimatableState>>.visible(
    canSeeBehind: (T) -> Boolean,
): List<Pair<T, ItemAnimatableState>> {
    return drop(
        withIndex()
            .indexOfLast { (index, element) ->
                // Element on top is never considered opaque, so animations are smooth the in disappears
                index < size - 1 && !canSeeBehind(element.first) && !element.second.isAnimating
            }.coerceAtLeast(0),
    )
}
