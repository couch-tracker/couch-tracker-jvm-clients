package com.github.couchtracker.jvmclients.common.ui.screen.addconnection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServer
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.data.api.CouchTrackerServerInfo
import com.github.couchtracker.jvmclients.common.navigation.*
import com.github.couchtracker.jvmclients.common.ui.component.ScreenOrPopup
import com.github.couchtracker.jvmclients.common.ui.component.TopAppBar
import kotlinx.datetime.Clock

object AddConnectionLocation : Location() {
    override val title = "Add connection"

    @Composable
    override fun title() {
    }

    @Composable
    override fun background() {
    }

    @Composable
    override fun content(
        database: Database,
        stackData: StackData<Location>,
        state: ItemAnimatableState,
        editStack: (StackData<Location>?) -> Unit
    ) {
        AddConnectionScreen(database, stackData, state, editStack)
    }
}

object AddConnectionStyle {

    @Composable
    fun Element(
        contentAlignment: Alignment = Alignment.TopStart,
        padding: PaddingValues = PaddingValues(16.dp),
        content: @Composable BoxScope.() -> Unit,
    ) {
        Box(Modifier.fillMaxWidth().padding(padding), contentAlignment = contentAlignment) {
            content()
        }
    }

    @Composable
    fun BoxElement(
        contentAlignment: Alignment = Alignment.TopStart,
        externalPadding: PaddingValues = PaddingValues(16.dp),
        internalPadding: PaddingValues = PaddingValues(16.dp),
        color: Color = MaterialTheme.colors.primary,
        content: @Composable BoxScope.() -> Unit,
    ) {
        Element(contentAlignment, externalPadding) {
            Surface(
                color = color.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(Modifier.fillMaxWidth().padding(internalPadding), contentAlignment = contentAlignment) {
                    content()
                }
            }
        }
    }

    @Composable
    fun ErrorMessage(error: String?) {
        Element(padding = PaddingValues(32.dp, 0.dp, 32.dp, 16.dp)) {
            Text(
                error ?: "Unknown error",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.error,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


sealed interface AddConnectionState : AppDestination {
    object ChooseServerState : AddConnectionState
    data class LoginState(
        val server: CouchTrackerServer,
        val serverInfo: CouchTrackerServerInfo,
    ) : AddConnectionState
}

@Composable
fun AddConnectionScreen(
    database: Database,
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit,
) {
    val goBackOrClose = editStack.popOrNull(stackData)
    var stack by remember { mutableStateOf(StackData.of<AddConnectionState>(AddConnectionState.ChooseServerState)) }

    ScreenOrPopup(state, goBackOrClose) { fill, width, height ->
        Column(if (fill) Modifier.fillMaxSize() else Modifier.width(640.dp)) {
            TopAppBar({ Text("Add connection") }, state, goBackOrClose, width, height)
            StackNavigation(
                stack,
                {
                    if (stack.contains(it)) {
                        stack = stack.pop(it)
                        true
                    } else false
                },
                modifier = if (fill) Modifier else Modifier.height(IntrinsicSize.Max),
            ) { destination, childState ->
                Surface(
                    modifier = Modifier.slideAnimation(childState, width),
                    color = MaterialTheme.colors.background
                ) {
                    when (destination) {
                        AddConnectionState.ChooseServerState -> ChooseServer(
                            Modifier.swipeToPop(state, width, height),
                        ) {
                            stack = stack.push(it)
                        }

                        is AddConnectionState.LoginState -> Login(
                            Modifier.swipeToPop(childState, width, height, horizontal = true, vertical = false),
                            { stack = stack.pop(destination) },
                            destination.server,
                            { server, authenticationInfo ->
                                val connection = CouchTrackerConnection(server, authenticationInfo.user.id)
                                database.userCacheQueries.upsert(
                                    authenticationInfo.user, Clock.System.now(), connection, authenticationInfo.user.id
                                )
                                database.couchTrackerCredentialsQueries.upsert(
                                    connection = connection,
                                    accessToken = authenticationInfo.accessToken,
                                    refreshToken = authenticationInfo.refreshToken,
                                )
                                goBackOrClose()
                            },
                        )
                    }
                }
            }
        }
    }
}