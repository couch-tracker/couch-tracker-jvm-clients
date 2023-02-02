package com.github.couchtracker.jvmclients.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.couchtracker.jvmclients.common.data.CouchTrackerUser

@Composable
fun ManageConnections(
    modifier: Modifier,
    connections: List<CouchTrackerUser>,
    onAdd: () -> Unit,
) {
    Box(modifier.background(MaterialTheme.colors.primary.copy(alpha = 0.1f))) {
        Button(onAdd, Modifier.align(Alignment.Center)) {
            Text("Add connection")
        }
    }
}