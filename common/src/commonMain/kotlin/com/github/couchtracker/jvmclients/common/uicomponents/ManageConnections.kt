@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.uicomponents

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
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.navigation.swipeToGoBack

@Composable
fun ManageConnections(
    modifier: Modifier,
    navigationData: NavigationData,
    connections: List<CouchTrackerConnection>,
    delete: (CouchTrackerConnection) -> Unit,
    onAdd: () -> Unit,
) {
    Column(modifier.swipeToGoBack(navigationData.manualAnimation)) {
        TopAppBar(
            { Text("Manage connections") },
            navigationData,
            swipeToGoBack = false,
        )
        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            items(connections) { conn ->
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${conn.server}: ${conn.id}")
                    IconButton({ delete(conn) }) {
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
        }
    }
}