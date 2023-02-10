@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.github.couchtracker.jvmclients.common.Location.*
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServer
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.navigation.AppDestination
import com.github.couchtracker.jvmclients.common.navigation.AppDestinationData
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.StackNavigation
import com.github.couchtracker.jvmclients.common.uicomponents.addconnection.AddConnection
import kotlinx.coroutines.Dispatchers

sealed class Location : AppDestination {
    object Home : Location()
    object ConnectionManagement : Location()
    object AddConnection : Location()
}

@Composable
fun App(
    driverFactory: DriverFactory,
    stackData: StackData<Location>,
    editStack: (StackData<Location>) -> Unit,
) {
    val database = remember {
        Database(
            driver = driverFactory.createDriver(),
            CouchTrackerConnectionAdapter = CouchTrackerConnection.Adapter(
                serverAdapter = CouchTrackerServer.dbAdapter
            )
        )
    }
    val stack by rememberUpdatedState(stackData)
    val connections by database.couchTrackerConnectionQueries.all()
        .asFlow()
        .mapToList(Dispatchers.Default)
        .collectAsState(emptyList())

    MaterialTheme(
        colors = CouchTrackerStyle.colors,
        shapes = CouchTrackerStyle.shapes,
    ) {
        CompositionLocalProvider(
            LocalElevationOverlay provides null
        ) {
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
                            delete = {
                                database.couchTrackerConnectionQueries.delete(server = it.server, id = it.id)
                            },
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
                            database.couchTrackerConnectionQueries.insert(login)
                            editStack(stack.popTo(l, ConnectionManagement))
                        }
                    }
                }
            }
        }
    }
}
