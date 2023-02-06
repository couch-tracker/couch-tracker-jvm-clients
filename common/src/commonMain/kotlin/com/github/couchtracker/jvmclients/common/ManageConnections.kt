@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common

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
    manualAnimation: SwipeableState<Boolean>,
    close: () -> Unit,
    connections: List<CouchTrackerUser>,
    change: (List<CouchTrackerUser>) -> Unit,
    onAdd: () -> Unit,
) {
    Column(modifier.swipeToGoBack(manualAnimation)) {
        TopAppBar(
            { Text("Manage connections") },
            modifier = Modifier,
            navigationIcon = {
                IconButton(close) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
        )
        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            items(connections) { conn ->
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${conn.server.address}: ${conn.id}")
                    IconButton({ change(connections.minus(conn)) }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp)) {
                    Button(onAdd, Modifier.align(Alignment.Center)) {
                        Text("Add connection")
                    }
                }
            }
            items(100){
                Text("$it $it $it $it ")
            }
        }
    }
}