package com.github.couchtracker.jvmclients.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.github.couchtracker.jvmclients.common.data.Database
import net.harawata.appdirs.AppDirsFactory
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