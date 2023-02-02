package com.github.couchtracker.jvmclients.common.data

data class CouchTrackerUser(
    val server: CouchTrackerServer,
    val id: String,
    val accessToken: String,
)