package com.github.couchtracker.jvmclients.common.navigation


interface AppDestination<T : AppDestination<T>> {
    val parent: T?
}

data class AppDestinationData(
    val opaque: Boolean = true,
)

data class StackData<T : AppDestination<T>>(
    val stack: List<T> = emptyList(),
) {

    fun pop(n: Int = 1) = StackData(stack.dropLast(n))
    fun push(item: T): StackData<T> {
        return StackData(stack.minus(item).plus(item))
    }

    fun popTo(item: T): StackData<T> {
        return pop().push(item)
    }

    fun popToParent(): StackData<T> {
        require(stack.isNotEmpty())
        val p = stack.last().parent
        return if (p == null) pop()
        else popTo(p)
    }

    companion object {
        fun <T : AppDestination<T>> of(home: T) = StackData(listOf(home))
    }
}