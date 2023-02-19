package com.github.couchtracker.jvmclients.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.*
import com.github.couchtracker.jvmclients.common.navigation.*
import com.github.couchtracker.jvmclients.common.ui.component.MainLayout
import com.github.couchtracker.jvmclients.common.ui.screen.addconnection.AddConnectionLocation
import com.github.couchtracker.jvmclients.common.utils.dbSerializerAdapter
import com.seiko.imageloader.LocalImageLoader

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

val LocalDataPortals = staticCompositionLocalOf { CouchTrackerDataPortals.empty }

@Composable
fun App(
    driverFactory: DriverFactory,
    stackData: StackData<Location>,
    editStack: (StackData<Location>?) -> Unit,
) {
    val database = remember {
        Database(
            driver = driverFactory.createDriver(),
            CouchTrackerCredentialsAdapter = CouchTrackerCredentials.Adapter(
                connectionAdapter = dbSerializerAdapter(),
                accessTokenAdapter = dbSerializerAdapter(),
                refreshTokenAdapter = dbSerializerAdapter(),
            ),
            UserCacheAdapter = UserCache.Adapter(
                connectionAdapter = dbSerializerAdapter(),
                userAdapter = dbSerializerAdapter(),
                downloadTimeAdapter = dbSerializerAdapter(),
            )
        )
    }
    val dataPortals by remember(database) { CouchTrackerDataPortals.getInstance(database) }
        .collectAsState(CouchTrackerDataPortals.empty)

    MaterialTheme(
        colors = CouchTrackerStyle.darkColors,
        shapes = CouchTrackerStyle.shapes,
    ) {
        CompositionLocalProvider(
            LocalImageLoader provides generateImageLoader(),
            LocalDataPortals provides dataPortals,
        ) {
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
