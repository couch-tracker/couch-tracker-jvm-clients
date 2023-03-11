package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.github.couchtracker.jvmclients.common.data.CachedValue

@Composable
fun <T> CachedValueContainer(modifier: Modifier, data: CachedValue<T>, f: @Composable (T) -> Unit) {
    Crossfade(data, modifier) {
        when (it) {
            is CachedValue.Loaded -> f(it.data)
            is CachedValue.NotLoaded -> NotLoadedContent(modifier, it)
        }
    }
}

@Composable
fun NotLoadedContent(modifier: Modifier, data: CachedValue.NotLoaded) {
    when (data) {
        is CachedValue.Error -> {
            // TODO: improve
            Column {
                Text(data.error.message ?: "Unknown error")
            }
        }

        CachedValue.Loading -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
