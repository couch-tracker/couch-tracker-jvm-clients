package com.github.couchtracker.jvmclients.common.data

import kotlinx.serialization.Serializable

@Serializable
data class CouchTrackerConnection(
    val server: CouchTrackerServer,
    val userId: String,
)
