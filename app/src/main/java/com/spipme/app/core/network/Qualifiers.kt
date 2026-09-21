package com.spipme.app.core.network

import javax.inject.Qualifier

/** Client OkHttp/Retrofit sans AuthInterceptor ni TokenAuthenticator — utilisé uniquement pour /auth/login et /auth/refresh. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClientBrut

/** Client OkHttp/Retrofit avec en-tête Authorization automatique + rafraîchissement sur 401 — utilisé pour tous les autres endpoints. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClientAuthentifie
