package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.auth.InscriptionRequestDto
import com.spipme.app.data.remote.dto.auth.InscriptionResponseDto
import com.spipme.app.data.remote.dto.auth.LoginRequestDto
import com.spipme.app.data.remote.dto.auth.LoginResponseDto
import com.spipme.app.data.remote.dto.auth.LogoutRequestDto
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

    /** Compte crÃ©Ã© INACTIF cÃ´tÃ© serveur â€” voir InscriptionResponseDto. */
    @POST("auth/register")
    suspend fun inscription(@Body body: InscriptionRequestDto): Response<InscriptionResponseDto>
}

