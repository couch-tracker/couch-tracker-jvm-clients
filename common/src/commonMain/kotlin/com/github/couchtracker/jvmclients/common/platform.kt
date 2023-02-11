package com.github.couchtracker.jvmclients.common

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

expect fun hasBackButton(): Boolean