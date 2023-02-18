package com.github.couchtracker.jvmclients.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServer
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.navigation.*
import com.github.couchtracker.jvmclients.common.uicomponents.*
import com.github.couchtracker.jvmclients.common.uicomponents.addconnection.AddConnectionLocation
import com.seiko.imageloader.LocalImageLoader
import kotlinx.coroutines.Dispatchers

abstract class Location : AppDestination {

    abstract val title: String?

    @Composable
    abstract fun title()

    @Composable
    abstract fun background()

    @Composable
    abstract fun content(
        database: Database,
        stackData: StackData<Location>,
        state: ItemAnimatableState,
        editStack: (StackData<Location>?) -> Unit,
    )
}

@Composable
fun App(
    driverFactory: DriverFactory,
    stackData: StackData<Location>,
    editStack: (StackData<Location>?) -> Unit,
) {
    val database = remember {
        Database(
            driver = driverFactory.createDriver(),
            CouchTrackerConnectionAdapter = CouchTrackerConnection.Adapter(
                serverAdapter = CouchTrackerServer.dbAdapter
            )
        )
    }
    val connections by database.couchTrackerConnectionQueries.all()
        .asFlow()
        .mapToList(Dispatchers.Default)
        .collectAsState(emptyList())

    MaterialTheme(
        colors = CouchTrackerStyle.darkColors,
        shapes = CouchTrackerStyle.shapes,
    ) {
        CompositionLocalProvider(LocalImageLoader provides generateImageLoader()) {
            val stack by rememberUpdatedState(stackData)
            BoxWithConstraints {
                val stackState = rememberStackState(
                    stack, {
                        if (stack.contains(it) && stack.canPop()) {
                            editStack(stack.pop(it))
                            true
                        } else false
                    }, { destination ->
                        destination == AddConnectionLocation && maxWidth >= 640.dp && maxHeight >= 640.dp
                    }
                )

                MainLayout(
                    stackState,
                    connections,
                    showPartialDrawer = maxWidth >= 640.dp,
                    resizeContent = maxWidth >= 960.dp,
                    addConnection = { editStack(stack.push(AddConnectionLocation)) },
                ) {
                    StackNavigationUI(stackState) { location, state ->
                        location.content(database, stack, state, editStack)
                    }
                }
            }
        }
    }
}
