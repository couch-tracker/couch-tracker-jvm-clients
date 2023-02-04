package com.github.couchtracker.jvmclients.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun <T> rememberStateFlow(data: T): MutableStateFlow<T> {
    val state = remember { MutableStateFlow(data) }
    state.value = data
    return state
}

@Composable
fun <I, O> flowTransform(
    data: I,
    initialData: O,
    mapping: (MutableStateFlow<I>) -> Flow<O>
): State<O> {
    val state = rememberStateFlow(data)
    return remember {
        mapping(state)
    }.collectAsState(initialData)
}