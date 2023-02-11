@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class NavigationData(
    val manualAnimation: SwipeableState<Boolean>,
    val scaffoldState: BackdropScaffoldState?,
    val isBottomOfStack: Boolean,
    val goBackOrClose: () -> Unit,
)

@Composable
fun MainLayout(
    content: @Composable (BackdropScaffoldState?) -> Unit,
) {
    val innerColors = lightColors()

    Scaffold { }
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
                    MainDrawer(Modifier.width(320.dp))
                }

                BackdropScaffold(
                    appBar = {
                        MainTopAppBar(ssOrNull)
                    },
                    scaffoldState = scaffoldState,
                    backLayerContent = { MainDrawer() },
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
) {
    Column(modifier) {
        ListItem(
            icon = { Icon(Icons.Default.Home, "Home") },
            text = { Text("Home") }
        )
    }
}