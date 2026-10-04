package com.spipme.app.data.remote.dto.core

import kotlinx.serialization.Serializable

/** Reflète exactement SecteurSerializer (apps.core) — id/code/nom uniquement. */
@Serializable
data class SecteurDto(
    val id: Int,
    val code: String,
    val nom: String,
)

