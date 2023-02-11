@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.github.couchtracker.jvmclients.common.navigation.*
import com.github.couchtracker.jvmclients.common.uicomponents.*
import com.github.couchtracker.jvmclients.common.uicomponents.addconnection.AddConnection
import kotlinx.coroutines.Dispatchers

sealed class Location : AppDestination {
    object Home : Location()
    object ConnectionManagement : Location()
    object AddConnection : Location()
    data class Show(val id: String) : Location()
}

@Composable
fun App(
    driverFactory: DriverFactory,
    stackData: StackData<Location>,
    editStack: (StackData<Location>) -> Unit,
    close: () -> Unit,
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
            MainLayout(
                connections,
                addConnection = { editStack(stack.push(AddConnection)) }
            ) { scaffoldState ->
                StackNavigation(
                    stackData,
                    {//TODO: this isn't right
                        if (stack.contains(it) && stack.canPop()) {
                            editStack(stack.pop(it))
                            true
                        } else false
                    },
                    { destination, w, h, isBottomOfStack ->
                        AppDestinationData(
                            opaque = destination != AddConnection || w < 640.dp || h < 640.dp,
                            isBottomOfStack = isBottomOfStack,
                        )
                    },
                ) { l, data, manualAnimation ->

                    val navigationData = NavigationData(
                        manualAnimation = manualAnimation,
                        scaffoldState = scaffoldState,
                        isBottomOfStack = data.isBottomOfStack,
                        goBackOrClose = {
                            if (stack.canPop()) editStack(stack.pop())
                            else close()
                        }
                    )

                    when (l) {
                        Home -> {
                            Screen {
                                Column(
                                    Modifier.fillMaxSize().swipeToGoBack(manualAnimation),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(Modifier.weight(1f))
                                    Button({
                                        editStack(stack.push(ConnectionManagement))
                                    }) {
                                        Text("Manage connections")
                                    }
                                    Button({
                                        editStack(stack.push(Show("tmdb:57243")))
                                    }) {
                                        Text("Open show")
                                    }
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        ConnectionManagement -> {
                            Screen {
                                ManageConnections(
                                    Modifier.fillMaxSize(),
                                    navigationData,
                                    connections = connections,
                                    delete = {
                                        database.couchTrackerConnectionQueries.delete(server = it.server, id = it.id)
                                    },
                                ) {
                                    editStack(stack.push(AddConnection))
                                }
                            }
                        }

                        AddConnection -> {
                            AddConnection(
                                Modifier.fillMaxSize(),
                                navigationData,
                                data.opaque,
                            ) { login ->
                                database.couchTrackerConnectionQueries.upsert(
                                    id = login.id,
                                    server = login.server,
                                    accessToken = login.accessToken,
                                    refreshToken = login.refreshToken,
                                )
                                editStack(stack.popTo(l, ConnectionManagement))
                            }
                        }

                        is Show -> {
                            Screen {
                                ShowScreen(
                                    Modifier.fillMaxSize(),
                                    navigationData,
                                    id = l.id,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
