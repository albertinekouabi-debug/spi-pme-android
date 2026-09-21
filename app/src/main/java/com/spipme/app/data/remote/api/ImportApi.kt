package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.imports.ApercuImportDto
import com.spipme.app.data.remote.dto.imports.ImportFichierDto
import com.spipme.app.data.remote.dto.registry.PageDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImportApi {
    @GET("imports/")
    suspend fun historique(
        @Query("secteur") secteurId: Int? = null,
        @Query("statut") statut: String? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<ImportFichierDto>>

    @Multipart
    @POST("imports/preview/")
    suspend fun previsualiser(
        @Part fichier: MultipartBody.Part,
        @Part("secteur") secteur: RequestBody,
    ): Response<ApercuImportDto>

    @Multipart
    @POST("imports/commit/")
    suspend fun importer(
        @Part fichier: MultipartBody.Part,
        @Part("secteur") secteur: RequestBody,
    ): Response<ImportFichierDto>
}
