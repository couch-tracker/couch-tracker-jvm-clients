package com.github.couchtracker.jvmclients.common.data.api

import kotlinx.serialization.Serializable

@Serializable
data class CouchTrackerServerInfo(
    val version: Int,
    val patch: Int,
    val name: String,
) {
    init {
        require(name == "couch-tracker")
        require(version > 0)
        require(patch >= 0)
    }
}
