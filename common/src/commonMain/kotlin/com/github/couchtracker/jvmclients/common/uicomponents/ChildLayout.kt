package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.navigation.swipeToGoBack

@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    navigationData: NavigationData,
) {
    var modifier = Modifier.fillMaxWidth()
    modifier = modifier.swipeToGoBack(navigationData.manualAnimation, true, true)
    TopAppBar(
        title = title,
        modifier = modifier,
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.background,
        navigationIcon = {
            if (navigationData.manualAnimation.canPop) {
                IconButton(navigationData.goBackOrClose) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        },
    )
}

@Composable
fun Screen(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit,
) {
    ScreenOrPopup(modifier, true, {}) {
        content()
    }
}

@Composable
fun ScreenOrPopup(
    modifier: Modifier = Modifier.fillMaxSize(),
    fill: Boolean,
    onDismiss: () -> Unit,
    content: @Composable (fill: Boolean) -> Unit,
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val w = this.maxWidth
        Box(
            Modifier.matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !fill,
                ) { onDismiss() },
        )

        Surface(
            if (fill) {
                Modifier.padding(MainLayoutChildrenSuggestedPadding.current)
            } else {
                Modifier.padding(32.dp)
            },
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