package com.spipme.app.data.remote.dto.auth

import com.spipme.app.data.remote.dto.UtilisateurDto
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val access: String,
    val refresh: String,
    val utilisateur: UtilisateurDto,
)

@Serializable
data class RefreshResponseDto(
    val access: String,
)

/**
 * Reflète le format d'erreur homogène produit par
 * apps.core.exceptions.spi_pme_exception_handler côté backend :
 * {"code": 400, "message": "...", "champs_invalides": {"champ": ["..."]} | null}
 */
@Serializable
data class ErreurApiDto(
    val code: Int? = null,
    val message: String? = null,
    @kotlinx.serialization.SerialName("champs_invalides") val champsInvalides: Map<String, List<String>>? = null,
)
