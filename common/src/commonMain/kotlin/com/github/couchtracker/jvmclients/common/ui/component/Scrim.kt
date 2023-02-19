package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun BoxScope.Scrim(
    visible: Boolean = true,
    onDismiss: () -> Unit
) {
    val dismissModifier = if (visible) {
        Modifier.clickable(remember { MutableInteractionSource() }, indication = null) { onDismiss() }
    } else Modifier
    Box(Modifier.matchParentSize().then(dismissModifier)) { }
}