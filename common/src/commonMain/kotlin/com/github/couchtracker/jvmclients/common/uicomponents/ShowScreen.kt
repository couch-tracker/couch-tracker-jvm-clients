package com.github.couchtracker.jvmclients.common.uicomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.couchtracker.jvmclients.common.navigation.swipeToGoBack

@Composable
fun ShowScreen(
    modifier: Modifier,
    navigationData: NavigationData,
    id: String,
) {
    Column(modifier.swipeToGoBack(navigationData.manualAnimation)) {
        TopAppBar(
            { Text("Show $id") },
            navigationData,
        )
    }
}