package com.github.couchtracker.jvmclients.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.github.couchtracker.jvmclients.common.data.Database
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import net.harawata.appdirs.AppDirsFactory
import okio.Path.Companion.toOkioPath
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val db = File(
            AppDirsFactory.getInstance().getUserDataDir("couch-tracker", "1.0", null, true),
            "db.sqlite"
        )
        db.parentFile.mkdirs()
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$db")
        if (!db.exists()) {
            Database.Schema.create(driver)
        }
        return driver
    }
}

actual fun hasBackButton(): Boolean = false

@Composable
actual fun generateImageLoader(): ImageLoader {
    return ImageLoader {
        components {
            setupDefaultComponents()
        }
        interceptor {
            memoryCacheConfig {
                maxSizePercent(0.25)
            }
            diskCacheConfig {
                val imagesCache = File(
                    AppDirsFactory.getInstance().getUserCacheDir("couch-tracker", "1.0", null),
                    "images"
                )
                imagesCache.parentFile.mkdirs()
                directory(imagesCache.toOkioPath())
                maxSizeBytes(512L * 1024 * 1024)
            }
        }
    }
}

@Composable
actual fun Modifier.systemBarsPadding(): Modifier {
    return this
}