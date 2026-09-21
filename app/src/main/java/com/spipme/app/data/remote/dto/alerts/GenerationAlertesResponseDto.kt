package com.spipme.app.data.remote.dto.alerts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerationAlertesResponseDto(
    val creees: List<AlerteDto> = emptyList(),
    @SerialName("resolues_automatiquement") val resoluesAutomatiquement: List<AlerteDto> = emptyList(),
)
