package com.github.couchtracker.jvmclients.common.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.CachedValue
import com.github.couchtracker.jvmclients.common.data.CouchTrackerDataPortal
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.data.api.ShowBasicInfo
import com.github.couchtracker.jvmclients.common.data.combine
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.ui.component.CachedValueContainer
import com.github.couchtracker.jvmclients.common.ui.component.FullscreenCard
import kotlinx.coroutines.flow.combine

private val shows = listOf(
    "tmdb:1705",
    "tmdb:57243",
    "tmdb:60059",
)

@Composable
fun HomeScreen(
    dataPortal: CouchTrackerDataPortal,
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit,
) {
    val showList: CachedValue<List<ShowBasicInfo>> by remember(dataPortal) {
        combine(shows.map { dataPortal.show(it) }) { s -> s.combine() }
    }.collectAsState(CachedValue.Loading)

    FullscreenCard(state) { w, h ->
        CachedValueContainer(Modifier.fillMaxSize(), showList) { sl ->
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(sl) { show ->
                    Button({ editStack(stackData.push(ShowLocation(show.id))) }) {
                        Text(show.name)
                    }
                }
            }
        }
    }
}

object HomeLocation : Location.Authenticated() {

    @Composable
    override fun titleAuthenticated(dataPortal: CouchTrackerDataPortal) {
        Text("Couch tracker")
    }

    @Composable
    override fun backgroundAuthenticated(dataPortal: CouchTrackerDataPortal) {
    }

    @Composable
    override fun contentAuthenticated(
        dataPortal: CouchTrackerDataPortal,
        database: Database,
        stackData: StackData<Location>,
        state: ItemAnimatableState,
        editStack: (StackData<Location>?) -> Unit,
    ) {
        HomeScreen(dataPortal, stackData, state, editStack)
    }
}
