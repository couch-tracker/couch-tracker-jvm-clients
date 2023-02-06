package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun PopupOrFill(
    modifier: Modifier,
    fill: Boolean,
    onDismiss: () -> Unit,
    content: @Composable (fill: Boolean) -> Unit,
) {
    Box(
        modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !fill,
                ) { onDismiss() },
        )

        Surface(
            Modifier.padding(if (fill) 0.dp else 32.dp),
            shape = if (fill) RectangleShape else MaterialTheme.shapes.large,
            color = MaterialTheme.colors.background,
            elevation = if (fill) 0.dp else 16.dp
        ) {
            content(fill)
        }
    }
}