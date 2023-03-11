package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.github.couchtracker.jvmclients.common.data.CachedValue

@Composable
fun NotLoadedContent(modifier: Modifier, data: CachedValue.NotLoaded){
    when(data){
        is CachedValue.Error -> TODO()
        CachedValue.Loading -> {
            Box(modifier, contentAlignment = Alignment.Center){
                CircularProgressIndicator()
            }
        }
    }
}
