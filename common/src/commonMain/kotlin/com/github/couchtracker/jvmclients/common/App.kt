@file:OptIn(ExperimentalAnimationApi::class)

package com.github.couchtracker.jvmclients.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.CouchTrackerCredentials
import com.github.couchtracker.jvmclients.common.data.CouchTrackerDataPortal
import com.github.couchtracker.jvmclients.common.data.CouchTrackerDataPortals
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.data.UserCache
import com.github.couchtracker.jvmclients.common.navigation.AppDestination
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.StackNavigationUI
import com.github.couchtracker.jvmclients.common.navigation.rememberStackState
import com.github.couchtracker.jvmclients.common.ui.component.MainLayout
import com.github.couchtracker.jvmclients.common.ui.screen.ManageConnectionLocation
import com.github.couchtracker.jvmclients.common.ui.screen.ReauthenticateLocation
import com.github.couchtracker.jvmclients.common.ui.screen.addconnection.AddConnectionLocation
import com.github.couchtracker.jvmclients.common.utils.dbSerializerAdapter
import com.seiko.imageloader.LocalImageLoader

@Composable
fun App(
    driverFactory: DriverFactory,
    stackData: StackData<Location>,
    editStack: (StackData<Location>?) -> Unit,
) {
    val scope = rememberCoroutineScope()
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
            ),
        )
    }
    val stack by rememberUpdatedState(stackData)
    val dataPortals by remember(database) {
        CouchTrackerDataPortals.getInstance(
            scope,
            database,
            reauthenticate = { conn ->
                editStack(stack.push(ReauthenticateLocation(conn)))
            },
        )
    }.collectAsState(null)
    MaterialTheme(
        colors = CouchTrackerStyle.darkColors,
        shapes = CouchTrackerStyle.shapes,
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            val transition = updateTransition(dataPortals)
            transition.Crossfade(contentKey = { it == null }) { dp ->
                if (dp != null) {
                    val realStack = if (dp.active == null) {
                        StackData(listOf<Location>(AddConnectionLocation))
                    } else {
                        stack
                    }
                    val realEditStack = if (dp.active == null) {
                        {}
                    } else {
                        editStack
                    }
                    AppWithAccounts(dp, realStack, realEditStack, database)
                }
            }
        }
    }
}

@Composable
private fun AppWithAccounts(
    dataPortals: CouchTrackerDataPortals,
    stack: StackData<Location>,
    editStack: (StackData<Location>?) -> Unit,
    database: Database,
) {
    val stack by rememberUpdatedState(stack)
    val editStack by rememberUpdatedState(editStack)
    CompositionLocalProvider(
        LocalImageLoader provides generateImageLoader(),
        LocalDataPortals provides dataPortals,
    ) {
        BoxWithConstraints {
            val stackState = rememberStackState(
                stack = stack,
                dismiss = {
                    if (stack.contains(it) && stack.canPop()) {
                        editStack(stack.pop(it))
                        true
                    } else {
                        false
                    }
                },
                canSeeBehind = { destination ->
                    destination != stack.bottomOrNull() && destination.popup && maxWidth >= 640.dp && maxHeight >= 640.dp
                },
            )

            MainLayout(
                dataPortals.active,
                stackState,
                showPartialDrawer = maxWidth >= 640.dp,
                resizeContent = maxWidth >= 960.dp,
                addConnection = { editStack(stack.push(AddConnectionLocation)) },
                manageConnection = { conn ->
                    editStack(stack.push(ManageConnectionLocation(conn)))
                },
            ) {
                StackNavigationUI(stackState) { location, state ->
                    location.content(dataPortals.active, database, stack, state, editStack)
                }
            }
        }
    }
}

abstract class Location : AppDestination {

    open val popup = false

    @Composable
    abstract fun title(dataPortal: CouchTrackerDataPortal?)

    @Composable
    abstract fun background(dataPortal: CouchTrackerDataPortal?)

    @Composable
    abstract fun content(
        dataPortal: CouchTrackerDataPortal?,
        database: Database,
        stackData: StackData<Location>,
        state: ItemAnimatableState,
        editStack: (StackData<Location>?) -> Unit,
    )

    abstract class Authenticated : Location() {
        @Composable
        override fun title(dataPortal: CouchTrackerDataPortal?) {
            if (dataPortal != null) titleAuthenticated(dataPortal)
        }

        @Composable
        abstract fun titleAuthenticated(dataPortal: CouchTrackerDataPortal)

        @Composable
        override fun background(dataPortal: CouchTrackerDataPortal?) {
            if (dataPortal != null) backgroundAuthenticated(dataPortal)
        }

        @Composable
        abstract fun backgroundAuthenticated(dataPortal: CouchTrackerDataPortal)

        @Composable
        override fun content(
            dataPortal: CouchTrackerDataPortal?,
            database: Database,
            stackData: StackData<Location>,
            state: ItemAnimatableState,
            editStack: (StackData<Location>?) -> Unit,
        ) {
            if (dataPortal != null) {
                contentAuthenticated(dataPortal, database, stackData, state, editStack)
            }
        }

        @Composable
        abstract fun contentAuthenticated(
            dataPortal: CouchTrackerDataPortal,
            database: Database,
            stackData: StackData<Location>,
            state: ItemAnimatableState,
            editStack: (StackData<Location>?) -> Unit,
        )
    }
}

val LocalDataPortals = staticCompositionLocalOf { CouchTrackerDataPortals.empty }
