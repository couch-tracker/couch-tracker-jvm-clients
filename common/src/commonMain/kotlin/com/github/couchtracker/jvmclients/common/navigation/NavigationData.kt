package com.github.couchtracker.jvmclients.common.navigation


interface AppDestination<T : AppDestination<T>> {
    val parent: T?
    val unique: Boolean get() = false
    val opaque: Boolean get() = true
}

data class StackItem<T : AppDestination<T>>(
    val destination: T,
    val discriminator: Int,
)

data class StackData<T : AppDestination<T>>(
    private val stack: List<T> = emptyList(),
) {
    val items = run {
        val done = mutableMapOf<T, Int>()
        stack.map { it ->
            done.compute(it) { _, prev ->
                prev?.plus(1) ?: 0
            }
            StackItem(it, done.getValue(it))
        }
    }

    fun pop(n: Int = 1) = StackData(stack.dropLast(n))
    fun push(item: T, unique: Boolean = item.unique, once: Boolean = false): StackData<T> {
        val ret = if (unique) stack.minus(item)
        else if (once && stack.lastOrNull() == item) return this
        else stack

        return StackData(ret.plus(item))
    }

    fun popTo(item: T): StackData<T> {
        return pop().push(item, once = true)
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