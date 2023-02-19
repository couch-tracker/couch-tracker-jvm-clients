package com.github.couchtracker.jvmclients.common.data.api

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val name: String,
)