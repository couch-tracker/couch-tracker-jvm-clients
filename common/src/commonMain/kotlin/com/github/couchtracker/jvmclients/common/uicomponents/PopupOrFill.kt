package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PopupOrFill(
    modifier: Modifier,
    onDismiss: () -> Unit,
    fill: (w: Dp, h: Dp) -> Boolean = { w, h -> w < 640.dp || h < 640.dp },
    content: @Composable (fill: Boolean) -> Unit,
) {
    BoxWithConstraints(
        modifier,
        contentAlignment = Alignment.Center
    ) {
        val fill = fill(this.maxWidth, this.maxHeight)

        Box(
            modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !fill,
            ) { onDismiss() },
        )


        Surface(
            Modifier.padding(if (fill) 0.dp else 32.dp),
            shape = if (fill) RectangleShape else MaterialTheme.shapes.large,
        ) {
            content(fill)
        }
    }
}