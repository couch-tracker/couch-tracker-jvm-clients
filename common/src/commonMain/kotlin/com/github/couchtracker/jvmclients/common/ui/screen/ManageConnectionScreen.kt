package com.github.couchtracker.jvmclients.common.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.LocalDataPortals
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.CachedValue
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.data.CouchTrackerDataPortal
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.data.api.User
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.popOrNull
import com.github.couchtracker.jvmclients.common.ui.component.ScreenCard
import com.github.couchtracker.jvmclients.common.ui.component.TopAppBar
import com.github.couchtracker.jvmclients.common.ui.screen.addconnection.AddConnectionStyle
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ManageConnectionScreen(
    connection: CouchTrackerConnection,
    database: Database,
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit,
) {
    val goBackOrClose = editStack.popOrNull(stackData)

    ScreenCard(state, goBackOrClose) { fill, width, height ->
        Column(if (fill) Modifier.fillMaxSize() else Modifier.width(640.dp).height(IntrinsicSize.Max)) {
            TopAppBar({ Text("Manage account") }, state, goBackOrClose, width, height)

            val portal = LocalDataPortals.current.portals.singleOrNull { it.connection == connection }
            val user: CachedValue<User> by remember(portal, connection) {
                portal?.user(connection.userId) ?: MutableStateFlow(CachedValue.Loading)
            }.collectAsState(CachedValue.Loading)
            val u = user
            AddConnectionStyle.BoxElement {
                Column {
                    Text("Server: ${connection.server.address}")
                    if (u is CachedValue.Loaded) {
                        Text("Email: ${u.data.email}")
                        Text("Username: ${u.data.username}")
                        if (u.data.name != null) {
                            Text("Name: ${u.data.name}")
                        }
                    }
                }
            }
            AddConnectionStyle.Element {
                // TODO: do better
                Column {
                    TextButton(
                        {
                            database.couchTrackerCredentialsQueries.deleteCredentials(connection)
                        },
                    ) {
                        Text("DELETE CREDENTIALS (DEBUG)", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                    TextButton(
                        {
                            // TODO: delete other data?
                            database.couchTrackerCredentialsQueries.delete(connection)
                            goBackOrClose()
                        },
                    ) {
                        Text("LOG OUT", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

data class ManageConnectionLocation(val connection: CouchTrackerConnection) : Location() {
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
        ManageConnectionScreen(connection, database, stackData, state, editStack)
    }
}
