package com.github.couchtracker.jvmclients.common.uicomponents.addconnection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.CouchTrackerConnection
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServer
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServerInfo
import com.github.couchtracker.jvmclients.common.navigation.*
import com.github.couchtracker.jvmclients.common.uicomponents.NavigationData
import com.github.couchtracker.jvmclients.common.uicomponents.ScreenOrPopup
import com.github.couchtracker.jvmclients.common.uicomponents.TopAppBar

object AddConnectionStyle {

    @Composable
    fun Element(
        contentAlignment: Alignment = Alignment.TopStart,
        padding: PaddingValues = PaddingValues(16.dp),
        content: @Composable BoxScope.() -> Unit,
    ) {
        Box(Modifier.fillMaxWidth().padding(padding), contentAlignment = contentAlignment) {
            content()
        }
    }

    @Composable
    fun BoxElement(
        contentAlignment: Alignment = Alignment.TopStart,
        externalPadding: PaddingValues = PaddingValues(16.dp),
        internalPadding: PaddingValues = PaddingValues(16.dp),
        color: Color = MaterialTheme.colors.primary,
        content: @Composable BoxScope.() -> Unit,
    ) {
        Element(contentAlignment, externalPadding) {
            Surface(
                color = color.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(Modifier.fillMaxWidth().padding(internalPadding), contentAlignment = contentAlignment) {
                    content()
                }
            }
        }
    }

    @Composable
    fun ErrorMessage(error: String?) {
        Element(padding = PaddingValues(32.dp, 0.dp, 32.dp, 16.dp)) {
            Text(
                error ?: "Unknown error",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.error,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


sealed interface AddConnectionState : AppDestination {
    object ChooseServerState : AddConnectionState
    data class LoginState(
        val server: CouchTrackerServer,
        val serverInfo: CouchTrackerServerInfo,
    ) : AddConnectionState
}

@Composable
fun AddConnection(
    modifier: Modifier,
    navigationData: NavigationData,
    addConnection: (CouchTrackerConnection) -> Unit,
) {
    var stack by remember { mutableStateOf(StackData.of<AddConnectionState>(AddConnectionState.ChooseServerState)) }

    ScreenOrPopup(navigationData.state, navigationData.goBackOrClose, modifier) { fill ->
        Column(if (fill) Modifier.fillMaxSize() else Modifier.width(640.dp)) {
            TopAppBar({ Text("Add connection") }, navigationData)
            StackNavigation(
                stack,
                {
                    if (stack.contains(it)) {
                        stack = stack.pop(it)
                        true
                    } else false
                },
                modifier = if (fill) Modifier else Modifier.height(IntrinsicSize.Max),//TODO: .animateContentSize(),
            ) { destination, state ->
                Surface(modifier = Modifier.slideAnimation(state), color = MaterialTheme.colors.background) {
                    when (destination) {
                        AddConnectionState.ChooseServerState -> ChooseServer(
                            Modifier.swipeToPop(navigationData.state),
                        ) {
                            stack = stack.push(it)
                        }

                        is AddConnectionState.LoginState -> Login(
                            Modifier.swipeToPop(state, horizontal = true, vertical = false),
                            { stack = stack.pop(destination) },
                            destination.server,
                            addConnection,
                        )
                    }
                }
            }
        }
    }
}
