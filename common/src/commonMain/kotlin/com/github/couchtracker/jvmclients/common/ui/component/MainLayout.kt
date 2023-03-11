@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.CouchTrackerStyle
import com.github.couchtracker.jvmclients.common.LocalDataPortals
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.CachedValue
import com.github.couchtracker.jvmclients.common.data.api.User
import com.github.couchtracker.jvmclients.common.multiplatformSystemBarsPadding
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackNavigationUI
import com.github.couchtracker.jvmclients.common.navigation.crossFade
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

internal val DRAWER_DEFAULT_WIDTH = 320.dp
private val drawerCollapsedWidth = 56.dp
private val drawerExpandedWidth = DRAWER_DEFAULT_WIDTH

@Composable
fun MainLayout(
    stackState: List<Pair<Location, ItemAnimatableState>>,
    showPartialDrawer: Boolean,
    resizeContent: Boolean,
    addConnection: () -> Unit,
    content: @Composable () -> Unit,
) {
    val cs = rememberCoroutineScope()
    val preferOpenDrawer = showPartialDrawer && resizeContent
    val drawerRevealed = remember { SwipeableState(preferOpenDrawer) }

    fun openDrawer() {
        cs.launch { drawerRevealed.animateTo(true) }
    }

    fun closeDrawer(force: Boolean) {
        if (force || !preferOpenDrawer) {
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
        Column(Modifier.multiplatformSystemBarsPadding().swipeForDrawer(drawerRevealed, drawerInitialVisibility)) {
            MainTopAppBar(drawerRevealed, stackState) {
                when {
                    drawerRevealed.targetValue -> closeDrawer(true)
                    else -> openDrawer()
                }
            }
            Box {
                MainDrawer(
                    Modifier
                        .width(DRAWER_DEFAULT_WIDTH).fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .swipeForDrawer(drawerRevealed, drawerInitialVisibility),
                    visibleWidth = { drawerInitialVisibilityPx + drawerRevealed.offset.value },
                ) {
                    addConnection()
                    closeDrawer(false)
                }
                val alpha by animateFloatAsState(
                    targetValue = if (drawerRevealed.targetValue && !resizeContent) 0.6f else 1f,
                    animationSpec = TweenSpec(),
                )
                val additionalPadding =
                    when {
                        resizeContent -> with(LocalDensity.current) { drawerRevealed.offset.value.toDp() }
                        else -> 0.dp
                    }
                Box(
                    Modifier
                        .graphicsLayer { this.alpha = alpha }
                        .padding(start = drawerInitialVisibility + additionalPadding, end = basePadding)
                        .offset {
                            when {
                                resizeContent -> IntOffset.Zero
                                else -> IntOffset(drawerRevealed.offset.value.roundToInt(), 0)
                            }
                        },
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
    drawerInitialVisibility: Dp,
): Modifier {
    val anchors = mapOf(
        0f to false,
        with(LocalDensity.current) { (drawerExpandedWidth - drawerInitialVisibility).toPx() } to true,
    )
    return swipeable(
        drawerRevealed,
        anchors,
        Orientation.Horizontal,
        resistance = SwipeableDefaults.resistanceConfig(anchors.keys, factorAtMin = 0f),
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
            StackNavigationUI(
                stackState,
                modifier = Modifier.offset {
                    IntOffset(drawerRevealed.offset.value.roundToInt(), 0)
                },
            ) { location, state ->
                Box(Modifier.crossFade(state, overlap = .75f)) { location.title() }
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
    visibleWidth: () -> Float,
    addConnection: () -> Unit,
) {
    val portals = LocalDataPortals.current

    LazyColumn(
        modifier
            .drawWithContent {
                val cds = this
                clipRect(0f, 0f, visibleWidth(), size.height) {
                    cds.drawContent()
                }
            },
    ) {
        items(portals.portals) { portal ->
            val user: CachedValue<User> by remember {
                // TODO it should be the portal that remembers
                portal.user(portal.connection.userId)
            }.collectAsState(CachedValue.Loading)
            ListItem(
                icon = { Icon(Icons.Default.AccountCircle, null) },
                text = {
                    Text(
                        when (val u = user) {
                            is CachedValue.Loading -> "Loading..."
                            is CachedValue.Error -> "Error: ${u.error.message}"
                            is CachedValue.Loaded -> u.data.username
                        },
                    )
                },
                secondaryText = {
                    Text(portal.connection.server.address, maxLines = 1, softWrap = false)
                },
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
