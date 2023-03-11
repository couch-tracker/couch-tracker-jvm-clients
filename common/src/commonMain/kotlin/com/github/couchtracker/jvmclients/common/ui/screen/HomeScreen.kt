package com.github.couchtracker.jvmclients.common.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.github.couchtracker.jvmclients.common.LocalDataPortals
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.CachedValue
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.data.api.ShowBasicInfo
import com.github.couchtracker.jvmclients.common.data.combine
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.swipeToPop
import com.github.couchtracker.jvmclients.common.ui.component.CachedValueContainer
import com.github.couchtracker.jvmclients.common.ui.component.Screen
import kotlinx.coroutines.flow.combine

private val shows = listOf(
    "tmdb:1705",
    "tmdb:57243",
    "tmdb:60059",
)

@Composable
fun HomeScreen(
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit,
) {
    val portals = LocalDataPortals.current
    val showList: CachedValue<List<ShowBasicInfo>> by remember(portals) {
        combine(shows.map { portals.show(it) }) { s -> s.combine() }
    }.collectAsState(CachedValue.Loading)

    Screen(state) { w, h ->
        CachedValueContainer(Modifier.fillMaxSize().swipeToPop(state, w, h), showList) { sl ->
            LazyColumn {
                items(sl) { show ->
                    Button({ editStack(stackData.push(ShowLocation(show.id))) }) {
                        Text(show.name)
                    }
                }
            }
        }
    }
}

object HomeLocation : Location() {

    @Composable
    override fun title() {
        Text("Couch tracker")
    }

    @Composable
    override fun background() {
    }

    @Composable
    override fun content(
        database: Database,
        stackData: StackData<Location>,
        state: ItemAnimatableState,
        editStack: (StackData<Location>?) -> Unit,
    ) {
        HomeScreen(stackData, state, editStack)
    }
}
