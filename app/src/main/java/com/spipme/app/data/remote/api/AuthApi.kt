package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.auth.ConfirmationReinitialisationRequestDto
import com.spipme.app.data.remote.dto.auth.DemandeReinitialisationRequestDto
import com.spipme.app.data.remote.dto.auth.InscriptionRequestDto
import com.spipme.app.data.remote.dto.auth.InscriptionResponseDto
import com.spipme.app.data.remote.dto.auth.LoginRequestDto
import com.spipme.app.data.remote.dto.auth.LoginResponseDto
import com.spipme.app.data.remote.dto.auth.LogoutRequestDto
import com.spipme.app.data.remote.dto.auth.MessageReponseDto
import com.spipme.app.data.remote.dto.auth.RefreshRequestDto
import com.spipme.app.data.remote.dto.auth.RefreshResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): Response<RefreshResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto): Response<Unit>

    /** Compte créé INACTIF côté serveur — voir InscriptionResponseDto. */
    @POST("auth/register")
    suspend fun inscription(@Body body: InscriptionRequestDto): Response<InscriptionResponseDto>

    /** Réponse 200 systématique côté serveur, même si l'email est inconnu (anti-énumération). */
    @POST("auth/password-reset/request")
    suspend fun demanderReinitialisation(@Body body: DemandeReinitialisationRequestDto): Response<MessageReponseDto>

    @POST("auth/password-reset/confirm")
    suspend fun confirmerReinitialisation(@Body body: ConfirmationReinitialisationRequestDto): Response<MessageReponseDto>
}

