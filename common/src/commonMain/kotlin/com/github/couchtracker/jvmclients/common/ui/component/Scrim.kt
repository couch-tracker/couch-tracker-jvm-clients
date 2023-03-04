package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

@Composable
fun BoxScope.Scrim(
    visible: Boolean = true,
    onDismiss: () -> Unit,
) {
    val dismissModifier = when {
        visible -> Modifier.clickable(remember { MutableInteractionSource() }, indication = null) { onDismiss() }
        else -> Modifier
    }
    Box(Modifier.matchParentSize().then(dismissModifier)) { }
}
