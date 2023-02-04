package com.github.couchtracker.jvmclients.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.CouchTrackerUser
import com.github.couchtracker.jvmclients.common.navigation.swipeToGoBack

@Composable
fun ManageConnections(
    modifier: Modifier,
    manualAnimation: Animatable<Float, AnimationVector1D>,
    close: () -> Unit,
    connections: List<CouchTrackerUser>,
    change: (List<CouchTrackerUser>) -> Unit,
    onAdd: () -> Unit,
) {
    Column(modifier.swipeToGoBack(manualAnimation, close)) {
        TopAppBar(
            { Text("Manage connections") },
            modifier = Modifier,
            navigationIcon = {
                IconButton(close) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
        )
        LazyColumn {
            items(connections) { conn ->
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${conn.server.address}: ${conn.id}")
                    IconButton({ change(connections.minus(conn)) }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
            item {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    Button(onAdd, Modifier.align(Alignment.Center)) {
                        Text("Add connection")
                    }
                }
            }
        }
    }
}