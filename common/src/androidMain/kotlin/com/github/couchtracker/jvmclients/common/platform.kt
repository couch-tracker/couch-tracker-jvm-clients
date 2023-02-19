package com.github.couchtracker.jvmclients.common

import android.content.Context
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.github.couchtracker.jvmclients.common.data.Database
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import okio.Path.Companion.toOkioPath

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(Database.Schema, context, "test.db")
    }
}

actual fun hasBackButton(): Boolean = true

@Composable
actual fun generateImageLoader(): ImageLoader {
    val context = LocalContext.current
    return ImageLoader {
        components {
            setupDefaultComponents(context)
        }
        interceptor {
            memoryCacheConfig {
                maxSizePercent(context, 0.25)
            }
            diskCacheConfig {
                directory(context.cacheDir.resolve("image_cache").toOkioPath())
                maxSizeBytes(512L * 1024 * 1024)
            }
        }
    }
}

@Composable
actual fun Modifier.multiplatformSystemBarsPadding(): Modifier {
    return systemBarsPadding()
}