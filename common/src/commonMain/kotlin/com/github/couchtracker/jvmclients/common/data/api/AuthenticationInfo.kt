package com.github.couchtracker.jvmclients.common.data.api

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationToken(
    val token: String,
    val expiration: Instant,
)

@Serializable
data class AuthenticationInfo(
    val accessToken: AuthenticationToken,
    val refreshToken: AuthenticationToken,
    val user: User,
)
