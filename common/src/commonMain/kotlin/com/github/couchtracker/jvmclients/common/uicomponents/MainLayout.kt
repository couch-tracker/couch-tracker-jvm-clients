@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.navigation.ManualAnimation
import kotlinx.coroutines.launch

data class NavigationData(
    val manualAnimation: ManualAnimation,
    val scaffoldState: BackdropScaffoldState?,
    val goBackOrClose: () -> Unit,
)

internal val DRAWER_DEFAULT_WIDTH = 320.dp
val MainLayoutChildrenSuggestedPadding = staticCompositionLocalOf { PaddingValues(0.dp) }

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
            Box {
                val drawerAlwaysOpen = w >= 1024.dp
                val scaffoldState = rememberBackdropScaffoldState(
                    BackdropValue.Concealed,
                    confirmStateChange = { v ->
                        !drawerAlwaysOpen || v == BackdropValue.Concealed
                    }
                )
                val ssOrNull = if (drawerAlwaysOpen) null else scaffoldState
                val basePadding = if (w < 640.dp) 8.dp else 16.dp
                val suggestedChildPadding = if (drawerAlwaysOpen) {
                    PaddingValues(start = DRAWER_DEFAULT_WIDTH + basePadding, end = basePadding)
                } else {
                    PaddingValues(horizontal = basePadding)
                }

                CompositionLocalProvider(
                    MainLayoutChildrenSuggestedPadding provides suggestedChildPadding
                ) {
                    BackdropScaffold(
                        modifier = Modifier.zIndex(1f),
                        appBar = {
                            val padding = if (drawerAlwaysOpen) MainLayoutChildrenSuggestedPadding.current
                            else PaddingValues(0.dp)
                            Box(Modifier.padding(padding)) {
                                MainTopAppBar(ssOrNull)
                            }
                        },
                        scaffoldState = scaffoldState,
                        backLayerContent = {
                            if (!drawerAlwaysOpen) {
                                MainDrawer(Modifier, connections, {
                                    cs.launch { scaffoldState.conceal() }
                                    addConnection()
                                }, false)
                            } else Spacer(Modifier.size(1.dp))
                        },
                        frontLayerContent = {
                            MaterialTheme(colors = innerColors) {
                                content(ssOrNull)
                            }
                        },
                        gesturesEnabled = !drawerAlwaysOpen,
                        backLayerBackgroundColor = Color.Transparent,
                        backLayerContentColor = MaterialTheme.colors.onBackground,
                        frontLayerBackgroundColor = if (drawerAlwaysOpen) Color.Transparent else MaterialTheme.colors.background,
                        frontLayerContentColor = innerColors.onBackground,
                        frontLayerShape = RectangleShape,
                        frontLayerScrimColor = MaterialTheme.colors.background.copy(alpha = 0.60f),
                        frontLayerElevation = 0.dp,
                    )

                    if (drawerAlwaysOpen) {
                        MainDrawer(
                            Modifier.width(DRAWER_DEFAULT_WIDTH).fillMaxHeight().align(Alignment.CenterStart),
                            connections,
                            addConnection,
                            true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTopAppBar(
    scaffoldState: BackdropScaffoldState?
) {
    val cs = rememberCoroutineScope()
    val toggleScaffold: (() -> Unit)? = if (scaffoldState == null) null else {
        {
            cs.launch {
                if (scaffoldState.isRevealed) {
                    scaffoldState.conceal()
                } else scaffoldState.reveal()
            }
        }
    }
    TopAppBar(
        modifier = Modifier.clickable(
            remember { MutableInteractionSource() },
            indication = null,
            enabled = toggleScaffold != null
        ) { toggleScaffold?.invoke() },
        title = { Text("Couch tracker") },
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp,
        navigationIcon = if (toggleScaffold != null) {
            {
                IconButton(toggleScaffold) {
                    Icon(Icons.Default.Menu, "Open navigation drawer")
                }
            }
        } else null)
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