@file:OptIn(ExperimentalAnimationApi::class)

package com.github.couchtracker.jvmclients.common

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServer
import com.github.couchtracker.jvmclients.common.data.CouchTrackerUser

@Composable
private fun Element(
    contentAlignment: Alignment = Alignment.TopStart,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.width(640.dp).padding(padding), contentAlignment = contentAlignment) {
        content()
    }
}

@Composable
private fun BoxElement(
    contentAlignment: Alignment = Alignment.TopStart,
    externalPadding: PaddingValues = PaddingValues(16.dp),
    internalPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Element(contentAlignment, externalPadding) {
        Surface(
            color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.2f)),
            shape = MaterialTheme.shapes.large,
        ) {
            Box(Modifier.fillMaxWidth().padding(internalPadding), contentAlignment = contentAlignment) {
                content()
            }
        }
    }
}

@Composable
fun AddConnection(
    modifier: Modifier,
    close: () -> Unit,
    addConnection: (CouchTrackerUser) -> Unit,
) {
    var server: CouchTrackerServer? by remember { mutableStateOf(null) }

    Column(modifier) {
        TopAppBar(
            { Text("Add connection") },
            navigationIcon = {
                IconButton(close) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        val slideSpec = tween<IntOffset>(CouchTrackerStyle.animationDuration.inWholeMilliseconds.toInt())
        AnimatedContent(
            targetState = server,
            transitionSpec = {
                if (targetState == null) {
                    slideIntoContainer(AnimatedContentScope.SlideDirection.End, slideSpec) with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.End, slideSpec)
                } else {
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Start, slideSpec) with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Start, slideSpec)
                }
            }
        ) { s ->
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (s == null) {
                    ChooseServer { server = it }
                } else {
                    Login(s, { server = null }, addConnection)
                }
            }
        }
    }
}

@Composable
private fun ChooseServer(
    onServerPicked: (CouchTrackerServer) -> Unit,
) {
    var serverAddress by remember { mutableStateOf("") }

    BoxElement {
        Column {
            Text("Couch Tracker is a decentralized service", style = MaterialTheme.typography.body1)
            Spacer(Modifier.height(8.dp))
            Text("To which server do you want to connect?", style = MaterialTheme.typography.body2)
        }
    }

    Element {
        TextField(
            serverAddress,
            { serverAddress = it },
            Modifier.fillMaxWidth(),
            placeholder = { Text("couch-tracker.myserver.com") }
        )
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
    Element(contentAlignment = Alignment.Center) {
        FloatingActionButton(
            { onServerPicked(CouchTrackerServer(serverAddress)) },
            backgroundColor = MaterialTheme.colors.primary,
        ) {
            Icon(Icons.Default.KeyboardArrowRight, "Next")
        }
    }
}

@Composable
private fun Login(
    server: CouchTrackerServer,
    onBack: () -> Unit,
    onLoginPicked: (CouchTrackerUser) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var passwork by remember { mutableStateOf("") }

    BoxElement {
        Text(
            "Login to ${server.address}",
            style = MaterialTheme.typography.body2,
        )
    }
    Element {
        Column {
            TextField(username, { username = it }, Modifier.fillMaxWidth(), placeholder = { Text("Username") })
            TextField(passwork, { passwork = it }, Modifier.fillMaxWidth(), placeholder = { Text("Password") })
        }
    }
    Element(contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FloatingActionButton(
                onBack,
                Modifier.size(40.dp),
                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, "Back")
            }
            Spacer(Modifier.width(16.dp))

            FloatingActionButton(
                { onLoginPicked(CouchTrackerUser(server, username, passwork)) },
                backgroundColor = MaterialTheme.colors.primary,
            ) {
                Icon(Icons.Default.Done, "Next")
            }

            Spacer(Modifier.width(56.dp))
        }
    }
}