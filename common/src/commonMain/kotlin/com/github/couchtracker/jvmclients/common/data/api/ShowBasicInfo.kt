package com.github.couchtracker.jvmclients.common.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ShowBasicInfo(
    val id: String,
    val name: String,
    val poster: Image?,
    val posterClean: Image?,
    val backdrop: Image?,
    val backdropClean: Image?,
) {
    val posterPreferClean = posterClean ?: poster
    val backdropPreferClean = backdropClean ?: backdrop
}
