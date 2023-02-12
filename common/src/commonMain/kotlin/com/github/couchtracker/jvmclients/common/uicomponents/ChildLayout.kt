package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.hasBackButton
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.stackAnimation
import com.github.couchtracker.jvmclients.common.navigation.swipeToGoBack

@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    navigationData: NavigationData,
) {
    var modifier = Modifier.fillMaxWidth()
    modifier = modifier.swipeToGoBack(navigationData.state, true, true)
    TopAppBar(
        title = title,
        modifier = modifier,
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.background,
        navigationIcon = if (!hasBackButton() && navigationData.state.canPop) {
            {
                IconButton(navigationData.goBackOrClose) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        } else null,
    )
}

@Composable
fun Screen(
    state: ItemAnimatableState,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit,
) {
    ScreenOrPopup(state, {}, modifier) {
        content()
    }
}

@Composable
fun ScreenOrPopup(
    state: ItemAnimatableState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (fill: Boolean) -> Unit,
) {
    val fill = state.isOpaque
    Box(modifier.stackAnimation(state), contentAlignment = Alignment.Center) {
        Scrim(!fill, color = Color.Transparent) {
            onDismiss()
        }

        Surface(
            Modifier.padding(if (!fill) 32.dp else 0.dp),
            shape = if (fill) screenShape() else MaterialTheme.shapes.large,
            color = MaterialTheme.colors.background,
            elevation = 16.dp
        ) {
            content(fill)
        }
    }
}

@Composable
private fun screenShape() = MaterialTheme.shapes.large.copy(
    bottomStart = CornerSize(0.dp),
    bottomEnd = CornerSize(0.dp),
)