@file:OptIn(ExperimentalMaterialApi::class)

package com.github.couchtracker.jvmclients.common.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.material.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.unit.*

class SwipeToPopNestedScrollConnection(
    private val state: ItemAnimatableState,
) : NestedScrollConnection {

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset = when {
        // If the user is swiping up, handle it
        available.y < 0 -> onScroll(available)
        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (source != NestedScrollSource.Drag) return Offset.Zero
        return onScroll(available)
    }

    private fun onScroll(available: Offset): Offset {
        println("OnScroll = ${available.y}")
        val consumed = state.verticalSwipe.performDrag(available.y)
        return Offset(x = 0f, y = consumed)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        println("OnFling = ${available.y}")
        state.verticalSwipe.performFling(available.y)
        return Velocity(0f, available.y)
    }
}
