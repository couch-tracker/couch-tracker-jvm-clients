package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun BoxScope.Scrim(
    visible: Boolean,
    color: Color = MaterialTheme.colors.background.copy(alpha = 0.6f),
    onDismiss: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = TweenSpec()
    )
    val dismissModifier = if (visible) {
        Modifier.pointerInput(Unit) { detectTapGestures { onDismiss() } }
    } else Modifier
    Canvas(
        Modifier
            .matchParentSize()
            .then(dismissModifier)
    ) {
        drawRect(color = color, alpha = alpha)
    }
}