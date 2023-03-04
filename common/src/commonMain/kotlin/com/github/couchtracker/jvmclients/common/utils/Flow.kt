package com.github.couchtracker.jvmclients.common.utils

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun <T> rememberStateFlow(
    data: T,
): MutableStateFlow<T> {
    val state = remember { MutableStateFlow(data) }
    state.value = data
    return state
}

@Composable
fun <I, O> rememberStateFlow(
    data: I,
    mapping: (MutableStateFlow<I>) -> Flow<O>,
): Flow<O> {
    val state = rememberStateFlow(data)
    return remember {
        mapping(state)
    }
}
