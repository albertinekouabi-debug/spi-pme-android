package com.spipme.app.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val identifiant: String,
    val password: String,
)

@Serializable
data class RefreshRequestDto(
    val refresh: String,
)

@Serializable
data class LogoutRequestDto(
    val refresh: String,
)
