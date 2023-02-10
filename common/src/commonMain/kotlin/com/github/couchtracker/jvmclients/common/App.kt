@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.Location.*
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.navigation.AppDestination
import com.github.couchtracker.jvmclients.common.navigation.AppDestinationData
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.StackNavigation
import com.github.couchtracker.jvmclients.common.uicomponents.addconnection.AddConnection

sealed class Location : AppDestination {
    object Home : Location()
    object ConnectionManagement : Location()
    object AddConnection : Location()
}

@Composable
fun App(
    stackData: StackData<Location>,
    editStack: (StackData<Location>) -> Unit,
) {
    val stack by rememberUpdatedState(stackData)
    MaterialTheme(
        colors = CouchTrackerStyle.colors,
        shapes = CouchTrackerStyle.shapes,
    ) {
        CompositionLocalProvider(
            LocalElevationOverlay provides null
        ) {
            var connections by remember { mutableStateOf(emptyList<CouchTrackerConnection>()) }
            StackNavigation(
                stackData,
                {//TODO: this isn't right
                    if (stack.contains(it)) {
                        editStack(stack.pop(it))
                        true
                    } else false
                },
                { destination, w, h ->
                    AppDestinationData(
                        opaque = destination != AddConnection || w < 640.dp || h < 640.dp
                    )
                },
            ) { l, data, manualAnimation ->
                when (l) {
                    Home -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Button({
                                editStack(stack.push(ConnectionManagement))
                            }) {
                                Text("Manage connections")
                            }
                        }
                    }

                    ConnectionManagement -> {
                        ManageConnections(
                            Modifier.fillMaxSize(),
                            manualAnimation,
                            close = { editStack(stack.pop(l)) },
                            connections = connections,
                            change = { connections = it },
                        ) {
                            editStack(stack.push(AddConnection))
                        }
                    }

                    AddConnection -> {
                        AddConnection(
                            Modifier.fillMaxSize(),
                            manualAnimation,
                            data.opaque,
                            { editStack(stack.pop(l)) }
                        ) { login ->
                            connections = connections.plus(login)
                            editStack(stack.popTo(l, ConnectionManagement))
                        }
                    }
                }
            }
        }
    }
}
