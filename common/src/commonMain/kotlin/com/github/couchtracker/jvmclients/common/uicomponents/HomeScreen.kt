package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.data.Database
import com.github.couchtracker.jvmclients.common.navigation.ItemAnimatableState
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.navigation.popOrNull
import com.github.couchtracker.jvmclients.common.navigation.swipeToPop

object HomeLocation : Location() {

    override val title = null

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
        editStack: (StackData<Location>?) -> Unit
    ) {
        HomeScreen(stackData, state, editStack)
    }
}

@Composable
fun HomeScreen(
    stackData: StackData<Location>,
    state: ItemAnimatableState,
    editStack: (StackData<Location>?) -> Unit
) {
    Screen(state) { w, h ->
        Column(
            Modifier.fillMaxSize().swipeToPop(state, w, h),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Button({
                editStack(stackData.push(ShowLocation("tmdb:57243")))
            }) {
                Text("Open show")
            }
            Spacer(Modifier.weight(1f))
        }
    }
}