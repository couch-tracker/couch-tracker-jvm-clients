@file:OptIn(ExperimentalAnimationApi::class)

package com.github.couchtracker.jvmclients.common

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.github.couchtracker.jvmclients.common.Location.*
import com.github.couchtracker.jvmclients.common.data.CouchTrackerUser

sealed class Location(val parent: Location?) {
    object ConnectionManagement : Location(null)
    object AddConnection : Location(ConnectionManagement)

    val depth: Int = parent?.depth?.plus(1) ?: 0
}

@Composable
fun App() {
    MaterialTheme(
        colors = CouchTrackerStyle.colors,
        shapes = CouchTrackerStyle.shapes,
    ) {
        Surface(Modifier.fillMaxSize(), color = CouchTrackerStyle.colors.background) {
            var location: Location by remember { mutableStateOf(ConnectionManagement) }
            var connections by remember { mutableStateOf(emptyList<CouchTrackerUser>()) }

            val duration = CouchTrackerStyle.animationDuration.inWholeMilliseconds.toInt()
            val scaleSpec = tween<Float>(duration)
            val slideSpec = tween<IntOffset>(duration)

            AnimatedContent(
                targetState = location,
                transitionSpec = {
                    if (targetState.depth > initialState.depth) {
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Start, slideSpec) with
                                scaleOut(scaleSpec, 0.9f)
                    } else {
                        scaleIn(scaleSpec, 0.9f) with
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.End, slideSpec)
                    }.apply {
                        targetContentZIndex = targetState.depth.toFloat()
                    }
                }
            ) { l ->
                Surface(Modifier.fillMaxSize()) {
                    when (l) {
                        ConnectionManagement -> {
                            ManageConnections(Modifier.fillMaxSize(), connections) {
                                location = AddConnection
                            }
                        }

                        AddConnection -> {
                            AddConnection(Modifier.fillMaxSize(), { location = ConnectionManagement }) { login ->
                                connections = connections.plus(login)
                                location = ConnectionManagement
                            }
                        }
                    }
                }
            }
        }
    }
}
