@file:OptIn(ExperimentalAnimationApi::class)

package com.github.couchtracker.jvmclients.common.ui.screen.addconnection

import androidx.compose.animation.*
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServer
import com.github.couchtracker.jvmclients.common.data.api.CouchTrackerServerInfo
import com.github.couchtracker.jvmclients.common.utils.rememberStateFlow
import io.ktor.client.plugins.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.mapLatest
import mu.KotlinLogging
import java.net.ConnectException
import java.nio.channels.UnresolvedAddressException


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
    modifier: Modifier,
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
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        logger.error(e) { "Error connecting to server '${server.address}'" }
                    }
                    ServerState.Error(server, e)
                }
            }
        }
    }.collectAsState(if (server.address.isBlank()) ServerState.Ignore(server) else ServerState.Loading(server))

    val ssTmp = serverStateState.value
    val serverState = if (ssTmp.server == server) ssTmp else ServerState.Loading(server)
    val serverStateTransition = updateTransition(targetState = serverState)
    Column(
        modifier.fillMaxSize().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                    contentKey = { it.javaClass }
                ) { serverState ->
                    when (serverState) {
                        is ServerState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                        is ServerState.Error -> LinearProgressIndicator(
                            1f, Modifier.fillMaxWidth(), color = MaterialTheme.colors.error
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
            } else Spacer(Modifier.fillMaxWidth())
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
                str, Modifier.clickable { uriHandler.openUri(remoteLink) },
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
            contentAlignment = Alignment.Center
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