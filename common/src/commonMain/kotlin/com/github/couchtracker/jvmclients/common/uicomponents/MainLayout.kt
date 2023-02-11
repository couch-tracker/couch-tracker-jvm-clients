@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.navigation.ManualAnimation
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class NavigationData(
    val manualAnimation: ManualAnimation,
    val scaffoldState: BackdropScaffoldState?,
    val goBackOrClose: () -> Unit,
)

internal val DRAWER_DEFAULT_WIDTH = 320.dp
private val drawerCollapsedWidth = 56.dp
private val drawerExpandedWidth = DRAWER_DEFAULT_WIDTH

@Composable
fun MainLayout(
    connections: List<CouchTrackerConnection>,
    addConnection: () -> Unit,
    content: @Composable (BackdropScaffoldState?) -> Unit,
) {
    val innerColors = lightColors()
    val cs = rememberCoroutineScope()
    val drawerRevealed = remember { SwipeableState(false) }

    BoxWithConstraints {
        val w = this.maxWidth
        val drawerPartiallyThere = w >= 640.dp
        val basePadding = if (drawerPartiallyThere) 16.dp else 8.dp
        val drawerInitialVisibility = if (drawerPartiallyThere) drawerCollapsedWidth else basePadding
        val drawerInitialVisibilityPx = with(LocalDensity.current) { drawerInitialVisibility.toPx() }
        Surface(color = MaterialTheme.colors.background) {
            Column(Modifier.swipeForDrawer(drawerRevealed, drawerInitialVisibility)) {
                MainTopAppBar(drawerRevealed, drawerPartiallyThere)
                Box {
                    MainDrawer(
                        Modifier
                            .width(DRAWER_DEFAULT_WIDTH).fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .swipeForDrawer(drawerRevealed, drawerInitialVisibility)
                            .clickable(remember { MutableInteractionSource() }, indication = null) {
                                cs.launch {
                                    drawerRevealed.animateTo(!drawerRevealed.currentValue)
                                }
                            },
                        connections,
                        visibleWidth = { drawerInitialVisibilityPx + drawerRevealed.offset.value },
                        addConnection = {
                            addConnection()
                            cs.launch { drawerRevealed.animateTo(false) }
                        },
                    )
                    Box(Modifier
                        .padding(start = drawerInitialVisibility, end = basePadding)
                        .offset { IntOffset(drawerRevealed.offset.value.roundToInt(), 0) }
                    ) {
                        MaterialTheme(colors = innerColors) {
                            content(null)
                        }
                        Scrim(drawerRevealed.targetValue) {
                            cs.launch { drawerRevealed.animateTo(false) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.swipeForDrawer(drawerRevealed: SwipeableState<Boolean>, drawerInitialVisibility: Dp): Modifier {
    val anchors = mapOf(
        0f to false,
        with(LocalDensity.current) { (drawerExpandedWidth - drawerInitialVisibility).toPx() } to true,
    )
    return swipeable(
        drawerRevealed,
        anchors,
        Orientation.Horizontal,
        resistance = SwipeableDefaults.resistanceConfig(anchors.keys, factorAtMin = 0f)
    )
}

@Composable
private fun MainTopAppBar(
    drawerRevealed: SwipeableState<Boolean>,
    drawerPartiallyThere: Boolean,
) {
    val cs = rememberCoroutineScope()
    val toggleScaffold: () -> Unit = {
        cs.launch {
            drawerRevealed.animateTo(!drawerRevealed.currentValue)
        }
    }
    TopAppBar(
        modifier = Modifier.clickable(
            remember { MutableInteractionSource() },
            indication = null,
            enabled = !drawerPartiallyThere,
            onClick = toggleScaffold,
        ),
        title = { Text("Couch tracker") },
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp,
        navigationIcon = {
            IconButton(toggleScaffold) {
                Icon(Icons.Default.Menu, "Open navigation drawer")
            }
        }
    )
}

@Composable
private fun MainDrawer(
    modifier: Modifier = Modifier,
    connections: List<CouchTrackerConnection>,
    visibleWidth: () -> Float,
    addConnection: () -> Unit,
) {
    LazyColumn(modifier
        .drawWithContent {
            val cds = this
            clipRect(0f, 0f, visibleWidth(), size.height) {
                cds.drawContent()
            }
        }) {
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