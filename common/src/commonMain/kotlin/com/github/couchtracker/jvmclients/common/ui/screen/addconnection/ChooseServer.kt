@file:OptIn(ExperimentalAnimationApi::class, ExperimentalCoroutinesApi::class)

package com.github.couchtracker.jvmclients.common.ui.screen.addconnection

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServer
import com.github.couchtracker.jvmclients.common.data.api.CouchTrackerServerInfo
import com.github.couchtracker.jvmclients.common.utils.rememberStateFlow
import io.ktor.client.plugins.ClientRequestException
import mu.KotlinLogging
import java.net.ConnectException
import java.nio.channels.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.mapLatest

private sealed interface ServerState {
    val server: CouchTrackerServer

    data class Ok(override val server: CouchTrackerServer, val info: CouchTrackerServerInfo) : ServerState
    data class Ignore(override val server: CouchTrackerServer) : ServerState
    data class Loading(override val server: CouchTrackerServer) : ServerState
    data class Error(override val server: CouchTrackerServer, val error: Exception) : ServerState {
        val errorMessage = when (error) {
            is UnresolvedAddressException -> "Unknown server"
            is ConnectException -> "Cannot contact server"
            is ClientRequestException -> "Server not configured correctly"
            else -> null
        }
    }
}

private val logger = KotlinLogging.logger("ChooseServer")

@Composable
fun ChooseServer(
    modifier: Modifier = Modifier,
    pushState: (AddConnectionState.LoginState) -> Unit,
) {
    var server by remember { mutableStateOf(CouchTrackerServer("")) }
    val serverStateState = rememberStateFlow(
        server,
    ) {
        it.mapLatest { server ->
            if (server.address.isBlank()) {
                ServerState.Ignore(server)
            } else {
                try {
                    delay(250)
                    ServerState.Ok(server, server.info())
                } catch (ce: CancellationException) {
                    ServerState.Error(server, ce)
                } catch (expected: Exception) {
                    logger.error(expected) { "Error connecting to server '${server.address}'" }
                    ServerState.Error(server, expected)
                }
            }
        }
    }.collectAsState(if (server.address.isBlank()) ServerState.Ignore(server) else ServerState.Loading(server))

    val ssTmp = serverStateState.value
    val serverState = if (ssTmp.server == server) ssTmp else ServerState.Loading(server)
    val serverStateTransition = updateTransition(targetState = serverState)
    Column(
        modifier.fillMaxSize().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AddConnectionStyle.BoxElement {
            Column {
                Text("Couch Tracker is a self-hosted service", style = MaterialTheme.typography.body1)
                Spacer(Modifier.height(8.dp))
                Text("To which server do you want to connect?", style = MaterialTheme.typography.body2)
            }
        }
        AddConnectionStyle.Element {
            Box {
                OutlinedTextField(
                    server.address,
                    { server = CouchTrackerServer(it.trim()) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Server address") },
                    singleLine = true,
                    placeholder = { Text("https://couch-tracker.myserver.com") },
                    isError = serverState is ServerState.Error,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions {
                        if (serverState is ServerState.Ok) {
                            pushState(AddConnectionState.LoginState(serverState.server, serverState.info))
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Dns, null)
                    },
                    shape = MaterialTheme.shapes.medium,
                )
                serverStateTransition.Crossfade(
                    Modifier.align(Alignment.BottomCenter),
                    contentKey = { it.javaClass },
                ) { serverState ->
                    when (serverState) {
                        is ServerState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                        is ServerState.Error -> LinearProgressIndicator(
                            progress = 1f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colors.error,
                        )

                        else -> {}
                    }
                }
            }
        }
        serverStateTransition.AnimatedContent(
            transitionSpec = {
                (fadeIn() with fadeOut()).using(SizeTransform(clip = false))
            },
        ) { serverState ->
            if (serverState is ServerState.Error) {
                AddConnectionStyle.ErrorMessage(serverState.errorMessage)
            } else {
                Spacer(Modifier.fillMaxWidth())
            }
        }
        AddConnectionStyle.Element(padding = PaddingValues(32.dp, 0.dp, 32.dp, 16.dp)) {
            val remoteLink = "https://github.com/couch-tracker/couch-tracker-server"
            val str = buildAnnotatedString {
                append("Want to host a server? Follow the instructions on ")
                pushStringAnnotation(tag = "couch-tracker-server", annotation = remoteLink)
                withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
                    append(remoteLink)
                }
                pop()
            }
            val uriHandler = LocalUriHandler.current
            Text(
                text = str,
                modifier = Modifier.clickable { uriHandler.openUri(remoteLink) },
                style = MaterialTheme.typography.subtitle2,
            )
        }
        serverStateTransition.AnimatedContent(
            contentKey = { it is ServerState.Ok },
            transitionSpec = {
                if (targetState is ServerState.Ok) {
                    scaleIn() with ExitTransition.None
                } else {
                    EnterTransition.None with scaleOut()
                }.using(SizeTransform(clip = false))
            },
            contentAlignment = Alignment.Center,
        ) { serverState ->
            if (serverState is ServerState.Ok) {
                AddConnectionStyle.Element(contentAlignment = Alignment.Center) {
                    FloatingActionButton(
                        { pushState(AddConnectionState.LoginState(serverState.server, serverState.info)) },
                        backgroundColor = MaterialTheme.colors.primary,
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, "Next")
                    }
                }
            }
        }
    }
}
