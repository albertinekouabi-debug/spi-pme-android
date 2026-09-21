package com.spipme.app.data.repository;

import com.spipme.app.core.security.SessionManager;
import com.spipme.app.core.security.TokenManager;
import com.spipme.app.data.local.dao.UtilisateurLocalDao;
import com.spipme.app.data.remote.api.AuthApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.serialization.json.Json;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<UtilisateurLocalDao> utilisateurLocalDaoProvider;

  private final Provider<Json> jsonProvider;

  public AuthRepositoryImpl_Factory(Provider<AuthApi> authApiProvider,
      Provider<TokenManager> tokenManagerProvider, Provider<SessionManager> sessionManagerProvider,
      Provider<UtilisateurLocalDao> utilisateurLocalDaoProvider, Provider<Json> jsonProvider) {
    this.authApiProvider = authApiProvider;
    this.tokenManagerProvider = tokenManagerProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.utilisateurLocalDaoProvider = utilisateurLocalDaoProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authApiProvider.get(), tokenManagerProvider.get(), sessionManagerProvider.get(), utilisateurLocalDaoProvider.get(), jsonProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<AuthApi> authApiProvider,
      Provider<TokenManager> tokenManagerProvider, Provider<SessionManager> sessionManagerProvider,
      Provider<UtilisateurLocalDao> utilisateurLocalDaoProvider, Provider<Json> jsonProvider) {
    return new AuthRepositoryImpl_Factory(authApiProvider, tokenManagerProvider, sessionManagerProvider, utilisateurLocalDaoProvider, jsonProvider);
  }

  public static AuthRepositoryImpl newInstance(AuthApi authApi, TokenManager tokenManager,
      SessionManager sessionManager, UtilisateurLocalDao utilisateurLocalDao, Json json) {
    return new AuthRepositoryImpl(authApi, tokenManager, sessionManager, utilisateurLocalDao, json);
  }
}
