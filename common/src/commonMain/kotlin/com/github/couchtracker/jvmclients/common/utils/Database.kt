package com.github.couchtracker.jvmclients.common.utils

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T : Any> dbSerializerAdapter() = object : ColumnAdapter<T, String> {
    override fun decode(databaseValue: String) = Json.decodeFromString<T>(databaseValue)
    override fun encode(value: T) = Json.encodeToString(value)
}
