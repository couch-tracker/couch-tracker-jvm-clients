@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.navigation.*
import com.seiko.imageloader.ImageRequestState
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.rememberAsyncImagePainter


data class ShowLocation(val id: String) : Location() {
    @Composable
    override fun title() {
        Text("Fringe")
    }

    @Composable
    override fun background() {
        val painter = rememberAsyncImagePainter(
            "https://www.themoviedb.org/t/p/original/wmF2N0mZT9pLHJB8w2Rocfq80j2.jpg",
            contentScale = ContentScale.Crop
        )
        Crossfade(painter.requestState) {
            if (it is ImageRequestState.Success) {
                Image(
                    painter, null,
                    Modifier.fillMaxSize().alpha(0.4f).blur(8.dp),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }

    @Composable
    override fun content(
        database: Database,
        stackData: StackData<Location>,
        state: ItemAnimatableState,
        editStack: (StackData<Location>?) -> Unit
    ) {
        ShowScreen(stackData, state, editStack, id)
    }
}


@Composable
fun ShowScreen(
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit,
    id: String,
) {
    BoxWithConstraints {
        val width = maxWidth
        val height = maxHeight
        Column(Modifier.swipeToPop(state, width, height)) {
            Text("  Insert      some    tabs    here", Modifier.crossFade(state))
            Spacer(Modifier.height(96.dp))
            ScreenCard(Modifier.stackAnimation(state, width, height)) {
                LazyColumn(Modifier.fillMaxSize()) {
                    item {
                        TopAppBar({ Text("Information") }, state, editStack.popOrNull(stackData), width, height)
                    }
                    items(1000) {
                        ListItem { Text("Item #$it") }
                    }
                }
            }
        }
    }
}