package com.github.couchtracker.jvmclients.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.Location.*
import com.github.couchtracker.jvmclients.common.data.CouchTrackerUser
import com.github.couchtracker.jvmclients.common.navigation.AppDestination
import com.github.couchtracker.jvmclients.common.navigation.AppDestinationData
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.StackNavigation

sealed class Location(
    override val parent: Location?,
) : AppDestination<Location> {

    object Home : Location(null)
    object ConnectionManagement : Location(Home)
    object AddConnection : Location(ConnectionManagement)
}

@Composable
fun App() {
    MaterialTheme(
        colors = CouchTrackerStyle.colors,
        shapes = CouchTrackerStyle.shapes,
    ) {
        CompositionLocalProvider(
            LocalElevationOverlay provides null
        ) {
            var stackData by remember { mutableStateOf(StackData.of(Home)) }
            var connections by remember { mutableStateOf(emptyList<CouchTrackerUser>()) }
            StackNavigation(
                stackData,
                { destination, w, h ->
                    AppDestinationData(
                        opaque = destination != AddConnection || w < 640.dp || h < 640.dp
                    )
                },
            ) { l, data ->
                when (l) {
                    Home -> {
                        Button({
                            stackData = stackData.push(ConnectionManagement)
                        }) {
                            Text("Manage connections")
                        }
                    }

                    ConnectionManagement -> {
                        ManageConnections(
                            Modifier.fillMaxSize(),
                            close = { stackData = stackData.popToParent() },
                            connections = connections,
                            change = { connections = it },
                        ) {
                            stackData = stackData.push(AddConnection)
                        }
                    }

                    AddConnection -> {
                        AddConnection(
                            Modifier.fillMaxSize(),
                            data.opaque,
                            { stackData = stackData.popToParent() }
                        ) { login ->
                            connections = connections.plus(login)
                            stackData = stackData.popTo(ConnectionManagement)
                        }
                    }
                }
            }
        }
    }
}
