package com.spipme.app.data.remote.dto.core

import kotlinx.serialization.Serializable

/** ReflÃ¨te exactement SecteurSerializer (apps.core) â€” id/code/nom uniquement. */
@Serializable
data class SecteurDto(
    val id: Int,
    val code: String,
    val nom: String,
)

