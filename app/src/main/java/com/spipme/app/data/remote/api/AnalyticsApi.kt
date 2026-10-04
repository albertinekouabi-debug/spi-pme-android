package com.spipme.app.data.remote.api

import com.spipme.app.data.remote.dto.analytics.CopiloteReponseDto
import com.spipme.app.data.remote.dto.analytics.CopiloteRequestDto
import com.spipme.app.data.remote.dto.analytics.InsightsReponseDto
import com.spipme.app.data.remote.dto.analytics.KpisDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AnalyticsApi {
    @GET("analytics/kpis")
    suspend fun kpis(@Query("secteur") secteurId: Int): Response<KpisDto>

    @GET("analytics/insights")
    suspend fun insights(@Query("secteur") secteurId: Int): Response<InsightsReponseDto>

    @POST("analytics/copilote")
    suspend fun copilote(@Body body: CopiloteRequestDto): Response<CopiloteReponseDto>
}
