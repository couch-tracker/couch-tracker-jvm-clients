package com.github.couchtracker.jvmclients.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.sqldelight.db.SqlDriver
import com.seiko.imageloader.ImageLoader

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

expect fun hasBackButton(): Boolean

@Composable
expect fun generateImageLoader(): ImageLoader

@Composable
expect fun Modifier.systemBarsPadding(): Modifier
