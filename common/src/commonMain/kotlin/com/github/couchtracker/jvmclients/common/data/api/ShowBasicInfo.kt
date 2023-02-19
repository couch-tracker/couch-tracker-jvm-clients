package com.github.couchtracker.jvmclients.common.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ShowBasicInfo(
    val id: String,
    val name: String,
)