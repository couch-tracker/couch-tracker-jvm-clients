package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
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
            Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.weight(1f))

                Icon(data.error.icon, null, Modifier.size(64.dp))
                Text(data.error.message, textAlign = TextAlign.Center)
                data.error.action?.let {
                    TextButton(it.action) {
                        Text(it.name)
                    }
                }

                Spacer(Modifier.weight(1f))
            }
        }

        CachedValue.Loading -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
