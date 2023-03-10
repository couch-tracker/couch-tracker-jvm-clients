package com.github.couchtracker.jvmclients.common.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.swipeToPop
import com.github.couchtracker.jvmclients.common.ui.component.Screen

@Composable
fun HomeScreen(
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit,
) {
    Screen(state) { w, h ->
        Column(
            Modifier.fillMaxSize().swipeToPop(state, w, h),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Button({ editStack(stackData.push(ShowLocation("tmdb:1705"))) }) {
                Text("Fringe")
            }
            Button({ editStack(stackData.push(ShowLocation("tmdb:57243"))) }) {
                Text("Doctor Who")
            }
            Button({ editStack(stackData.push(ShowLocation("tmdb:60059"))) }) {
                Text("Better Call Saul")
            }
            Spacer(Modifier.weight(1f))
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
