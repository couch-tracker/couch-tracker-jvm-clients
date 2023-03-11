package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.hasBackButton
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.SwipeToPopNestedScrollConnection
import com.github.couchtracker.jvmclients.common.navigation.stackAnimation
import com.github.couchtracker.jvmclients.common.navigation.swipeToPop

@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    state: ItemAnimatableState,
    goBackOrClose: () -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
    swipeable: Boolean = true,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor),
    showBackButton: Boolean = !hasBackButton() && state.canPop,
) {
    var m = modifier.fillMaxWidth()
    if (swipeable) {
        m = m.swipeToPop(state, screenWidth, screenHeight, true, true)
    }
    TopAppBar(
        title = title,
        modifier = m,
        elevation = 0.dp,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        navigationIcon = if (showBackButton) {
            {
                IconButton(goBackOrClose) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        } else {
            null
        },
    )
}

@Composable
fun FullscreenCard(
    state: ItemAnimatableState,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (w: Dp, h: Dp) -> Unit,
) {
    ScreenCard(state, { }, modifier) { _, w, h ->
        content(w, h)
    }
}

@Composable
fun ScreenCard(
    state: ItemAnimatableState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (fill: Boolean, w: Dp, h: Dp) -> Unit,
) {
    val fill = state.isOpaque
    BoxWithConstraints {
        val w = maxWidth
        val h = maxHeight
        Box(modifier.stackAnimation(state, w, h), contentAlignment = Alignment.Center) {
            Scrim(!fill, onDismiss)
            ScreenCard(Modifier, state, w, h, fill) {
                content(fill, w, h)
            }
        }
    }
}

@Composable
private fun ScreenCard(
    modifier: Modifier = Modifier,
    state: ItemAnimatableState,
    width: Dp,
    height: Dp,
    fullscreen: Boolean = true,
    content: @Composable () -> Unit,
) {
    val nestedScrollConnection = remember(state) {
        SwipeToPopNestedScrollConnection(state)
    }
    Surface(
        modifier
            .padding(if (!fullscreen) 32.dp else 0.dp)
            .swipeToPop(state, width, height, true, false)
            .nestedScroll(connection = nestedScrollConnection),
        shape = when {
            fullscreen -> MaterialTheme.shapes.large.copy(
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp),
            )

            else -> MaterialTheme.shapes.large
        },
        color = MaterialTheme.colors.background,
        elevation = 16.dp,
    ) {
        content()
    }
}
