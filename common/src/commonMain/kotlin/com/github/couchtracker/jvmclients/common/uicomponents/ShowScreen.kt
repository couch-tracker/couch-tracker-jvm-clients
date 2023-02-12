package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.couchtracker.jvmclients.common.navigation.swipeToPop

@Composable
fun ShowScreen(
    modifier: Modifier,
    navigationData: NavigationData,
    id: String,
) {
    Column(modifier.swipeToPop(navigationData.state)) {
        TopAppBar(
            { Text("Show $id") },
            navigationData,
        )
    }
}