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
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.CachedValue
import com.github.couchtracker.jvmclients.common.data.CouchTrackerDataPortal
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.data.api.ShowBasicInfo
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.crossFade
import com.github.couchtracker.jvmclients.common.ui.component.CachedValueContainer
import com.github.couchtracker.jvmclients.common.ui.component.FadeInImage
import com.github.couchtracker.jvmclients.common.ui.component.FullscreenCard
import com.seiko.imageloader.rememberAsyncImagePainter

@Composable
fun ShowScreen(
    dataPortal: CouchTrackerDataPortal,
    state: ItemAnimatableState,
    id: String,
) {
    Column {
        Text("Insert      some    tabs    here", Modifier.crossFade(state))
        Spacer(Modifier.height(96.dp).fillMaxWidth())
        FullscreenCard(state) { w, h ->
            val show by remember(dataPortal) {
                dataPortal.show(id)
            }.collectAsState(CachedValue.Loading)
            CachedValueContainer(Modifier.fillMaxSize(), show) { s ->
                LoadedContent(s, w, h)
            }
        }
    }
}

@Composable
private fun LoadedContent(
    show: ShowBasicInfo,
    width: Dp,
    height: Dp,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            ShowHeader(show, width, height)
        }
        items(100) {
            ListItem { Text("Item #$it") }
        }
    }
}

@Composable
private fun ShowHeader(
    show: ShowBasicInfo,
    width: Dp,
    height: Dp,
) {
    Row(Modifier) {
        if (show.posterPreferClean != null) {
            val painter = rememberAsyncImagePainter(
                show.posterPreferClean.sourceFor(width, height).url,
                contentScale = ContentScale.Crop,
            )
            FadeInImage(
                painter,
                Modifier.width(minOf(240.dp, width * 2 / 5)).aspectRatio(2 / 3f),
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

data class ShowLocation(val id: String) : Location.Authenticated() {

    @Composable
    override fun titleAuthenticated(dataPortal: CouchTrackerDataPortal) {
        val show by dataPortal.show(id).collectAsState(CachedValue.Loading)
        Crossfade(show) { s ->
            if (s is CachedValue.Loaded) {
                Text(s.data.name)
            }
        }
    }

    @Composable
    override fun backgroundAuthenticated(dataPortal: CouchTrackerDataPortal) {
        val show by dataPortal.show(id).collectAsState(CachedValue.Loading)
        when (val s = show) {
            is CachedValue.Loaded -> {
                if (s.data.backdropPreferClean != null) {
                    BoxWithConstraints {
                        val painter = rememberAsyncImagePainter(
                            s.data.backdropPreferClean.sourceFor(maxWidth, maxHeight).url,
                            contentScale = ContentScale.Crop,
                        )
                        FadeInImage(
                            painter,
                            Modifier.fillMaxSize().alpha(0.4f),
                            springStiffness = Spring.StiffnessLow,
                        )
                    }
                }
            }

            else -> {}
        }
    }

    @Composable
    override fun contentAuthenticated(
        dataPortal: CouchTrackerDataPortal,
        database: Database,
        stackData: StackData<Location>,
        state: ItemAnimatableState,
        editStack: (StackData<Location>?) -> Unit,
    ) {
        ShowScreen(dataPortal, state, id)
    }
}
