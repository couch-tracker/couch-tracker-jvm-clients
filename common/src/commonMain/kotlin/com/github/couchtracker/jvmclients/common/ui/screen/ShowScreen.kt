@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.LocalDataPortals
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.CachedValue
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.data.api.ShowBasicInfo
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.crossFade
import com.github.couchtracker.jvmclients.common.navigation.popOrNull
import com.github.couchtracker.jvmclients.common.navigation.stackAnimation
import com.github.couchtracker.jvmclients.common.navigation.swipeToPop
import com.github.couchtracker.jvmclients.common.ui.component.CachedValueContainer
import com.github.couchtracker.jvmclients.common.ui.component.FadeInImage
import com.github.couchtracker.jvmclients.common.ui.component.NotLoadedContent
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
    BoxWithConstraints {
        val width = maxWidth
        val height = maxHeight
        Column(Modifier.swipeToPop(state, width, height)) {
            Text("  Insert      some    tabs    here", Modifier.crossFade(state))
            Spacer(Modifier.height(96.dp))
            ScreenCard(Modifier.stackAnimation(state, width, height)) {
                val show by LocalDataPortals.current.show(id).collectAsState(CachedValue.Loading)
                CachedValueContainer(Modifier.fillMaxSize(), show) { s ->
                    LoadedContent(s, stackData, state, editStack, width, height)
                }
            }
        }
    }
}

@Composable
private fun LoadedContent(
    show: ShowBasicInfo,
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit,
    width: Dp,
    height: Dp,
) {
    LazyColumn(Modifier.fillMaxSize()) {
//        item {
//            TopAppBar({ Text("Information") }, state, editStack.popOrNull(stackData), width, height)
//        }
        item {
            ShowHeader(show, state, width, height)
        }
        items(1000) {
            ListItem { Text("Item #$it") }
        }
    }
}

@Composable
private fun ShowHeader(
    show: ShowBasicInfo,
    state: ItemAnimatableState,
    width: Dp,
    height: Dp,
) {
    Row(Modifier.swipeToPop(state, width, height, true, true)) {
        if (show.posterClean != null) {
            val painter = rememberAsyncImagePainter(
                show.posterClean.url,
                contentScale = ContentScale.Crop,
            )
            FadeInImage(
                painter,
                Modifier.width(minOf(240.dp, width * 2 / 5)).aspectRatio(2 / 3f),
                springStiffness = Spring.StiffnessHigh,
            )
        }
        Column {
            TopAppBar(
                title = { Text(show.name) },
                elevation = 0.dp,
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colors.onBackground,
            )
        }
    }
}

data class ShowLocation(val id: String) : Location() {

    @Composable
    override fun title() {
        val show by LocalDataPortals.current.show(id).collectAsState(CachedValue.Loading)
        Crossfade(show) { s ->
            if (s is CachedValue.Loaded) {
                Text(s.data.name)
            }
        }
    }

    @Composable
    override fun background() {
        val show by LocalDataPortals.current.show(id).collectAsState(CachedValue.Loading)
        when (val s = show) {
            is CachedValue.Loaded -> {
                if (s.data.backdropClean != null) {
                    val painter = rememberAsyncImagePainter(
                        s.data.backdropClean.url,
                        contentScale = ContentScale.Crop,
                    )
                    FadeInImage(painter, Modifier.fillMaxSize().alpha(0.4f))
                }
            }

            else -> {}
        }
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
