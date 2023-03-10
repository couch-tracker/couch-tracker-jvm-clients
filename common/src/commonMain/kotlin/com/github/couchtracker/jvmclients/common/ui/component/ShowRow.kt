package com.github.couchtracker.jvmclients.common.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.data.api.ShowBasicInfo
import com.seiko.imageloader.rememberAsyncImagePainter

@Composable
fun ShowRow(
    modifier: Modifier,
    show: ShowBasicInfo,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (show.posterPreferClean != null) {
            val width = 56.dp
            val painter = rememberAsyncImagePainter(
                show.posterPreferClean.sourceFor(width = width).url,
                contentScale = ContentScale.Crop,
            )
            FadeInImage(
                painter,
                Modifier.width(width).aspectRatio(2 / 3f),
            )
        }
        Text(show.name, Modifier.padding(16.dp), style = MaterialTheme.typography.body1)
    }
}
