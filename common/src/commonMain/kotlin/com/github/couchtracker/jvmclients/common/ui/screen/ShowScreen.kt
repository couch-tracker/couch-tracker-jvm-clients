@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.LocalDataPortals
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.crossFade
import com.github.couchtracker.jvmclients.common.navigation.popOrNull
import com.github.couchtracker.jvmclients.common.navigation.stackAnimation
import com.github.couchtracker.jvmclients.common.navigation.swipeToPop
import com.github.couchtracker.jvmclients.common.ui.component.FadeInImage
import com.github.couchtracker.jvmclients.common.ui.component.ScreenCard
import com.github.couchtracker.jvmclients.common.ui.component.TopAppBar
import com.seiko.imageloader.rememberAsyncImagePainter

@Composable
fun ShowScreen(
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit,
    id: String,
) {
    val activePortal = LocalDataPortals.current.active
    LaunchedEffect(id, activePortal) {
        if (activePortal != null) {
            try {
                println(activePortal.show(id))
            } catch (expected: Exception) {
                expected.printStackTrace()
            }
        }
    }

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

data class ShowLocation(val id: String) : Location() {

    override val title = "Fringe"

    @Composable
    override fun title() {
        Text("Fringe")
    }

    @Composable
    override fun background() {
        val painter = rememberAsyncImagePainter(
            "https://www.themoviedb.org/t/p/original/wmF2N0mZT9pLHJB8w2Rocfq80j2.jpg",
            contentScale = ContentScale.Crop,
        )
        FadeInImage(painter, Modifier.fillMaxSize().alpha(0.4f).blur(8.dp))
    }

    @Composable
    override fun content(
        database: Database,
        stackData: StackData<Location>,
        state: ItemAnimatableState,
        editStack: (StackData<Location>?) -> Unit,
    ) {
        ShowScreen(stackData, state, editStack, id)
    }
}
