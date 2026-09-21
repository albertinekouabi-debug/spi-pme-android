package com.spipme.app.core.network

import com.spipme.app.BuildConfig
import com.spipme.app.data.remote.api.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun fournirJson(): Json = Json {
        ignoreUnknownKeys = true   // le backend peut ajouter des champs sans casser l'app
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun fournirLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

    /** Client SANS AuthInterceptor ni TokenAuthenticator — réservé à /auth/login et /auth/refresh. */
    @Provides
    @Singleton
    @ClientBrut
    fun fournirOkHttpClientBrut(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @ClientBrut
    fun fournirRetrofitBrut(@ClientBrut client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @ClientBrut
    fun fournirAuthApiBrut(@ClientBrut retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    /** Client AVEC AuthInterceptor + TokenAuthenticator — utilisé pour tous les autres endpoints métier. */
    @Provides
    @Singleton
    @ClientAuthentifie
    fun fournirOkHttpClientAuthentifie(
        logging: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            // Ce client gère l'upload multipart du module imports (fichiers
            // CSV/XLSX pouvant atteindre plusieurs Mo) — le défaut OkHttp
            // (10s) serait trop court sur un réseau lent.
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @ClientAuthentifie
    fun fournirRetrofitAuthentifie(@ClientAuthentifie client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    /**
     * AuthApi exposé aussi via le client authentifié, pour /auth/logout
     * (qui exige d'être connecté — contrairement à login/refresh).
     */
    @Provides
    @Singleton
    fun fournirAuthApi(@ClientAuthentifie retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun fournirEntiteApi(@ClientAuthentifie retrofit: Retrofit): com.spipme.app.data.remote.api.EntiteApi =
        retrofit.create(com.spipme.app.data.remote.api.EntiteApi::class.java)

    @Provides
    @Singleton
    fun fournirRessourceApi(@ClientAuthentifie retrofit: Retrofit): com.spipme.app.data.remote.api.RessourceApi =
        retrofit.create(com.spipme.app.data.remote.api.RessourceApi::class.java)

    @Provides
    @Singleton
    fun fournirTransactionApi(@ClientAuthentifie retrofit: Retrofit): com.spipme.app.data.remote.api.TransactionApi =
        retrofit.create(com.spipme.app.data.remote.api.TransactionApi::class.java)

    @Provides
    @Singleton
    fun fournirTacheApi(@ClientAuthentifie retrofit: Retrofit): com.spipme.app.data.remote.api.TacheApi =
        retrofit.create(com.spipme.app.data.remote.api.TacheApi::class.java)

    @Provides
    @Singleton
    fun fournirSuggestionApi(@ClientAuthentifie retrofit: Retrofit): com.spipme.app.data.remote.api.SuggestionApi =
        retrofit.create(com.spipme.app.data.remote.api.SuggestionApi::class.java)

    @Provides
    @Singleton
    fun fournirAlerteApi(@ClientAuthentifie retrofit: Retrofit): com.spipme.app.data.remote.api.AlerteApi =
        retrofit.create(com.spipme.app.data.remote.api.AlerteApi::class.java)

    @Provides
    @Singleton
    fun fournirImportApi(@ClientAuthentifie retrofit: Retrofit): com.spipme.app.data.remote.api.ImportApi =
        retrofit.create(com.spipme.app.data.remote.api.ImportApi::class.java)
}
