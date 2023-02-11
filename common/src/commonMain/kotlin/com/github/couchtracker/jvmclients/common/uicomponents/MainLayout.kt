@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import kotlinx.coroutines.launch

data class NavigationData(
    val manualAnimation: SwipeableState<Boolean>,
    val scaffoldState: BackdropScaffoldState?,
    val isBottomOfStack: Boolean,
    val goBackOrClose: () -> Unit,
)

@Composable
fun MainLayout(
    connections: List<CouchTrackerConnection>,
    addConnection: () -> Unit,
    content: @Composable (BackdropScaffoldState?) -> Unit,
) {
    val innerColors = lightColors()
    val cs = rememberCoroutineScope()
    BoxWithConstraints {
        val w = this.maxWidth
        Surface(color = MaterialTheme.colors.background) {
            Row {
                val drawerAlwaysOpen = w >= 1024.dp
                val scaffoldState = rememberBackdropScaffoldState(
                    BackdropValue.Concealed,
                    confirmStateChange = { v ->
                        !drawerAlwaysOpen || v == BackdropValue.Concealed
                    }
                )
                val ssOrNull = if (drawerAlwaysOpen) null else scaffoldState

                if (drawerAlwaysOpen) {
                    MainDrawer(Modifier.width(320.dp), connections, addConnection, true)
                }

                BackdropScaffold(
                    appBar = {
                        MainTopAppBar(ssOrNull)
                    },
                    scaffoldState = scaffoldState,
                    backLayerContent = {
                        MainDrawer(Modifier, connections, {
                            cs.launch { scaffoldState.conceal() }
                            addConnection()
                        }, false)
                    },
                    frontLayerContent = {
                        MaterialTheme(colors = innerColors) {
                            content(ssOrNull)
                        }
                    },
                    gesturesEnabled = !drawerAlwaysOpen,
                    backLayerBackgroundColor = MaterialTheme.colors.background,
                    frontLayerBackgroundColor = MaterialTheme.colors.background,
                    frontLayerContentColor = innerColors.onBackground,
                    frontLayerShape = RectangleShape,
                    frontLayerScrimColor = MaterialTheme.colors.background.copy(alpha = 0.60f),
                )
            }
        }
    }
}

@Composable
private fun MainTopAppBar(
    scaffoldState: BackdropScaffoldState?
) {
    val cs = rememberCoroutineScope()
    TopAppBar(
        modifier = Modifier, title = { Text("Couch tracker") },
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp,
        navigationIcon = if (scaffoldState == null) null else {
            {
                IconButton({
                    cs.launch {
                        if (scaffoldState.isRevealed) {
                            scaffoldState.conceal()
                        } else scaffoldState.reveal()
                    }
                }) {
                    Icon(Icons.Default.Menu, "Open navigation drawer")
                }
            }
        })
}

@Composable
private fun MainDrawer(
    modifier: Modifier = Modifier,
    connections: List<CouchTrackerConnection>,
    addConnection: () -> Unit,
    isInline: Boolean,
) {
    LazyColumn(modifier, contentPadding = PaddingValues(vertical = if (isInline) 56.dp else 0.dp)) {
        items(connections) { conn ->
            ListItem(
                icon = { Icon(Icons.Default.AccountCircle, null) },
                text = { Text(conn.id) },
                secondaryText = {
                    Text(conn.server.address, maxLines = 1, softWrap = false)
                }
            )
        }
        item {
            ListItem(
                modifier = Modifier.clickable { addConnection() },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add account") },
            )
        }
        item { Divider() }
        item {
            ListItem(
                icon = { Icon(Icons.Default.Home, "Home") },
                text = { Text("Home") },
            )
        }
    }
}