@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.CouchTrackerStyle
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackNavigationUI
import com.github.couchtracker.jvmclients.common.navigation.crossFade
import com.github.couchtracker.jvmclients.common.systemBarsPadding
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal val DRAWER_DEFAULT_WIDTH = 320.dp
private val drawerCollapsedWidth = 56.dp
private val drawerExpandedWidth = DRAWER_DEFAULT_WIDTH

@Composable
fun MainLayout(
    stackState: List<Pair<Location, ItemAnimatableState>>,
    connections: List<CouchTrackerConnection>,
    showPartialDrawer: Boolean,
    resizeContent: Boolean,
    addConnection: () -> Unit,
    content: @Composable () -> Unit,
) {
    val cs = rememberCoroutineScope()
    var forceOpen by remember { mutableStateOf(false) }
    val drawerRevealed = remember {
        SwipeableState(
            false,
            confirmStateChange = {
                forceOpen = it
                true
            }
        )
    }

    fun openDrawer(force: Boolean = false) {
        if (force) forceOpen = true
        cs.launch { drawerRevealed.animateTo(true) }
    }

    fun closeDrawer(force: Boolean = false) {
        if (force) forceOpen = false
        if (!forceOpen) {
            cs.launch { drawerRevealed.animateTo(false) }
        }
    }
    Surface(color = MaterialTheme.colors.background) {
        StackNavigationUI(stackState) { location, state ->
            Box(Modifier.crossFade(state)) { location.background() }
        }
        val basePadding = if (showPartialDrawer) 16.dp else 8.dp
        val drawerInitialVisibility = if (showPartialDrawer) drawerCollapsedWidth else basePadding
        val drawerInitialVisibilityPx = with(LocalDensity.current) { drawerInitialVisibility.toPx() }
        Column(Modifier.systemBarsPadding().swipeForDrawer(drawerRevealed, drawerInitialVisibility)) {
            MainTopAppBar(drawerRevealed, stackState) {
                if (drawerRevealed.targetValue) closeDrawer(true)
                else openDrawer(true)
            }
            Box {
                MainDrawer(
                    Modifier
                        .width(DRAWER_DEFAULT_WIDTH).fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .swipeForDrawer(drawerRevealed, drawerInitialVisibility)
                        .then(if (showPartialDrawer) {
                            Modifier.pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
                                        when (event.type) {
                                            PointerEventType.Enter -> openDrawer()
                                            PointerEventType.Exit -> closeDrawer()
                                        }
                                    }
                                }
                            }
                        } else Modifier),
                    connections,
                    visibleWidth = { drawerInitialVisibilityPx + drawerRevealed.offset.value },
                ) {
                    addConnection()
                    if (!showPartialDrawer) {
                        closeDrawer(true)
                    }
                }
                val alpha by animateFloatAsState(
                    targetValue = if (drawerRevealed.targetValue && !resizeContent) 0.6f else 1f,
                    animationSpec = TweenSpec()
                )
                val additionalPadding =
                    if (resizeContent) with(LocalDensity.current) { drawerRevealed.offset.value.toDp() }
                    else 0.dp
                Box(Modifier
                    .graphicsLayer { this.alpha = alpha }
                    .padding(start = drawerInitialVisibility + additionalPadding, end = basePadding)
                    .offset {
                        if (resizeContent) IntOffset.Zero
                        else IntOffset(drawerRevealed.offset.value.roundToInt(), 0)
                    }
                ) {
                    MaterialTheme(colors = CouchTrackerStyle.lightColors) {
                        content()
                    }
                    if (!resizeContent) {
                        Scrim(drawerRevealed.targetValue) {
                            closeDrawer(true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.swipeForDrawer(
    drawerRevealed: SwipeableState<Boolean>,
    drawerInitialVisibility: Dp
): Modifier {
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
    stackState: List<Pair<Location, ItemAnimatableState>>,
    toggleDrawer: () -> Unit,
) {
    TopAppBar(
        title = {
            Row {
                Spacer(Modifier.width(with(LocalDensity.current) { drawerRevealed.offset.value.toDp() }))
                StackNavigationUI(stackState) { location, state ->
                    Box(Modifier.crossFade(state, overlap = .75f)) { location.title() }
                }
            }
        },
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colors.onBackground,
        elevation = 0.dp,
        navigationIcon = {
            IconButton(toggleDrawer) {
                Icon(Icons.Default.Menu, "Navigation drawer")
            }
        },
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