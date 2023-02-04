@file:OptIn(ExperimentalAnimationApi::class)

package com.github.couchtracker.jvmclients.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServer
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServerInfo
import com.github.couchtracker.jvmclients.common.data.CouchTrackerUser
import com.github.couchtracker.jvmclients.common.navigation.swipeToGoBack
import com.github.couchtracker.jvmclients.common.uicomponents.PopupOrFill
import com.github.couchtracker.jvmclients.common.utils.blend
import com.github.couchtracker.jvmclients.common.utils.flowTransform
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*


@Composable
private fun Element(
    contentAlignment: Alignment = Alignment.TopStart,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.fillMaxWidth().padding(padding), contentAlignment = contentAlignment) {
        content()
    }
}

@Composable
private fun BoxElement(
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
private fun shaped(content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
    ) {
        content()
    }
}

private sealed interface AddConnectionState {
    data class ChooseServerState(
        val server: CouchTrackerServer
    ) : AddConnectionState {

        fun nextPage(serverInfo: CouchTrackerServerInfo): LoginState {
            return LoginState(server, serverInfo)
        }
    }

    data class LoginState(
        val server: CouchTrackerServer,
        val serverInfo: CouchTrackerServerInfo,
    ) : AddConnectionState {

        fun prevPage(): ChooseServerState {
            return ChooseServerState(server)
        }
    }
}

@Composable
fun AddConnection(
    modifier: Modifier,
    manualAnimation: Animatable<Float, AnimationVector1D>,
    fill: Boolean,
    close: () -> Unit,
    addConnection: (CouchTrackerUser) -> Unit,
) {
    var state by remember { mutableStateOf<AddConnectionState>(AddConnectionState.ChooseServerState(CouchTrackerServer(""))) }

    PopupOrFill(modifier, fill, close) { fill ->
        Column(if (fill) Modifier.fillMaxSize() else Modifier.width(640.dp)) {
            TopAppBar(
                { Text("Add connection") },
                modifier = Modifier.swipeToGoBack(manualAnimation, close),
                navigationIcon = {
                    IconButton(close) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
            val slideSpec = tween<IntOffset>(CouchTrackerStyle.animationDuration.inWholeMilliseconds.toInt())
            val transition = updateTransition(targetState = state)
            transition.AnimatedContent(
                contentKey = { it.javaClass },
                transitionSpec = {
                    if (targetState is AddConnectionState.ChooseServerState) {
                        slideIntoContainer(AnimatedContentScope.SlideDirection.End, slideSpec) with
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.End, slideSpec)
                    } else {
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Start, slideSpec) with
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.Start, slideSpec)
                    }.using(SizeTransform(clip = false))
                }
            ) { p ->
                Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    when (p) {
                        is AddConnectionState.ChooseServerState -> {
                            ChooseServer(p) { state = it }
                        }

                        is AddConnectionState.LoginState -> {
                            Login(p, { state = it }, addConnection)
                        }
                    }
                }
            }
        }
    }
}

private sealed interface ServerState {
    val server: CouchTrackerServer

    data class Ok(override val server: CouchTrackerServer, val info: CouchTrackerServerInfo) : ServerState
    data class Error(override val server: CouchTrackerServer, val error: Exception) : ServerState
    data class Ignore(override val server: CouchTrackerServer) : ServerState
    data class Loading(override val server: CouchTrackerServer) : ServerState
}

@Composable
private fun ChooseServer(
    state: AddConnectionState.ChooseServerState,
    changeState: (AddConnectionState) -> Unit,
) {
    val serverStateState = flowTransform(
        state.server,
        if (state.server.address.isBlank()) ServerState.Ignore(state.server) else ServerState.Loading(state.server)
    ) {
        it.mapLatest { server ->
            if (server.address.isBlank()) {
                ServerState.Ignore(server)
            } else {
                try {
                    delay(250)
                    println("Downloading ${server.address}")
                    ServerState.Ok(server, server.info())
                } catch (e: Exception) {
                    ServerState.Error(server, e)
                }
            }
        }
    }
    val ssTmp = serverStateState.value
    val serverState = if (ssTmp.server == state.server) ssTmp else ServerState.Loading(state.server)
    val serverStateTransition = updateTransition(targetState = serverState)

    BoxElement {
        Column {
            Text("Couch Tracker is a decentralized service", style = MaterialTheme.typography.body1)
            Spacer(Modifier.height(8.dp))
            Text("To which server do you want to connect?", style = MaterialTheme.typography.body2)
        }
    }
    Element {
        shaped {
            Box {
                val focusRequester = FocusRequester()
                TextField(
                    state.server.address,
                    { changeState(AddConnectionState.ChooseServerState(CouchTrackerServer(it.trim()))) },
                    Modifier.fillMaxWidth().focusRequester(focusRequester),
                    label = { Text("Server address") },
                    singleLine = true,
                    placeholder = { Text("https://couch-tracker.myserver.com") },
                    isError = serverState is ServerState.Error,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions {
                        if (serverState is ServerState.Ok) {
                            changeState(state.nextPage(serverState.info))
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Dns, null)
                    },
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
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
    }
    serverStateTransition.AnimatedContent(
        transitionSpec = {
            (fadeIn() with fadeOut()).using(SizeTransform(clip = false))
        },
    ) { serverState ->
        if (serverState is ServerState.Error) {
            Element(padding = PaddingValues(32.dp, 0.dp, 32.dp, 16.dp)) {
                Text(
                    serverState.error.message ?: "Unknown error",
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else Spacer(Modifier.fillMaxWidth())
    }
    Element(padding = PaddingValues(32.dp, 0.dp, 32.dp, 16.dp)) {
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
            Element(contentAlignment = Alignment.Center) {
                FloatingActionButton(
                    { changeState(state.nextPage(serverState.info)) },
                    backgroundColor = MaterialTheme.colors.primary,
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, "Next")
                }
            }
        }
    }
}

@Composable
private fun Login(
    state: AddConnectionState.LoginState,
    changeState: (AddConnectionState) -> Unit,
    onLoginPicked: (CouchTrackerUser) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var passwork by remember { mutableStateOf("") }

    BoxElement {
        Text(
            "Login to ${state.server.address}",
            style = MaterialTheme.typography.body2,
        )
    }
    Element {
        shaped {
            Column {
                val focusManager = LocalFocusManager.current
                val focusRequester = FocusRequester()
                TextField(
                    username,
                    { username = it },
                    Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    label = { Text("Username or email") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
                    keyboardActions = KeyboardActions {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.AccountCircle, null)
                    },
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                var isPasswordVisible by remember { mutableStateOf(false) }
                TextField(
                    passwork,
                    { passwork = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (!isPasswordVisible) PasswordVisualTransformation()
                    else VisualTransformation.None,
                    trailingIcon = {
                        IconButton({ isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Password Visibility"
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Password, null)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
                )
            }
        }
    }
    Element(contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FloatingActionButton(
                { changeState(state.prevPage()) },
                Modifier.size(40.dp),
                backgroundColor = MaterialTheme.colors.primary.blend(MaterialTheme.colors.background, 0.8f),
                contentColor = MaterialTheme.colors.primary,
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, "Back")
            }
            Spacer(Modifier.width(16.dp))

            FloatingActionButton(
                { onLoginPicked(CouchTrackerUser(state.server, username, passwork)) },
                backgroundColor = MaterialTheme.colors.primary,
            ) {
                Icon(Icons.Default.Done, "Next")
            }

            Spacer(Modifier.width(56.dp))
        }
    }
}