package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.ChangerMotDePasseRequestDto
import com.spipme.app.data.remote.dto.MettreAJourProfilRequestDto
import com.spipme.app.data.remote.dto.UtilisateurDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface MoiApi {
    @GET("me")
    suspend fun obtenirProfil(): Response<UtilisateurDto>

    /** Le serveur ignore/refuse silencieusement tout champ hors nom_complet/telephone (MoiUpdateSerializer). */
    @PATCH("me")
    suspend fun mettreAJourProfil(@Body body: MettreAJourProfilRequestDto): Response<UtilisateurDto>

    @POST("me/change-password")
    suspend fun changerMotDePasse(@Body body: ChangerMotDePasseRequestDto): Response<Unit>
}

