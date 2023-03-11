package com.github.couchtracker.jvmclients.common.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.LocalDataPortals
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.CachedValue
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.data.CouchTrackerDataPortal
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.popOrNull
import com.github.couchtracker.jvmclients.common.ui.component.ScreenCard
import com.github.couchtracker.jvmclients.common.ui.component.TopAppBar
import com.github.couchtracker.jvmclients.common.ui.screen.addconnection.Login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

@Composable
fun ReauthenticateScreen(
    connection: CouchTrackerConnection,
    database: Database,
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit,
) {
    val goBackOrClose = editStack.popOrNull(stackData)

    ScreenCard(state, goBackOrClose) { fill, width, height ->
        Column(if (fill) Modifier.fillMaxSize() else Modifier.width(640.dp).height(IntrinsicSize.Max)) {
            TopAppBar({ Text("Re-authenticate") }, state, goBackOrClose, width, height)
            val portal = LocalDataPortals.current.portals.singleOrNull { it.connection == connection }
            val login: String by remember(portal, connection) {
                portal?.user(connection.userId)?.map {
                    when (it) {
                        is CachedValue.Loaded -> it.data.email
                        is CachedValue.NotLoaded -> connection.userId
                    }
                } ?: MutableStateFlow(connection.userId)
            }.collectAsState(connection.userId)

            Login(
                Modifier,
                back = null,
                server = connection.server,
                forcedLogin = login,
                onLogin = { _, authenticationInfo ->
                    // TODO: retry downloads
                    authenticationInfo.save(connection, database)
                    goBackOrClose()
                },
            )
        }
    }
}

data class ReauthenticateLocation(val connection: CouchTrackerConnection) : Location() {
    override val popup = true

    @Composable
    override fun title(dataPortal: CouchTrackerDataPortal?) {
    }

    @Composable
    override fun background(dataPortal: CouchTrackerDataPortal?) {
    }

    @Composable
    override fun content(
        dataPortal: CouchTrackerDataPortal?,
        database: Database,
        stackData: StackData<Location>,
        state: ItemAnimatableState,
        editStack: (StackData<Location>?) -> Unit,
    ) {
        ReauthenticateScreen(connection, database, stackData, state, editStack)
    }
}
