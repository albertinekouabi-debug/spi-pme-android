package com.spipme.app.data.remote.api

import retrofit2.http.Header
import com.spipme.app.data.remote.dto.registry.PageDto
import com.spipme.app.data.remote.dto.treasury.CreerTransactionRequestDto
import com.spipme.app.data.remote.dto.treasury.PointSoldeDto
import com.spipme.app.data.remote.dto.treasury.ResumeTresorerieDto
import com.spipme.app.data.remote.dto.treasury.TransactionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TransactionApi {
    @GET("transactions/")
    suspend fun lister(
        @Query("secteur") secteurId: Int? = null,
        @Query("type") type: String? = null,
        @Query("entite") entiteId: Int? = null,
        @Query("ressource") ressourceId: Int? = null,
        @Query("page") page: Int? = null,
    ): Response<PageDto<TransactionDto>>

    @GET("transactions/summary/")
    suspend fun resume(@Query("secteur") secteurId: Int? = null): Response<ResumeTresorerieDto>

    @GET("transactions/evolution/")
    suspend fun evolution(@Query("secteur") secteurId: Int? = null, @Query("jours") jours: Int = 7): Response<List<PointSoldeDto>>

    @POST("transactions/")
    suspend fun creer(@Header("Idempotency-Key") cle: String? = null, @Body body: CreerTransactionRequestDto): Response<TransactionDto>
}
