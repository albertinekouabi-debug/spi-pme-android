package com.spipme.app.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.spipme.app.core.network.ClientAuthentifie")
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
public final class NetworkModule_FournirOkHttpClientAuthentifieFactory implements Factory<OkHttpClient> {
  private final Provider<HttpLoggingInterceptor> loggingProvider;

  private final Provider<AuthInterceptor> authInterceptorProvider;

  private final Provider<TokenAuthenticator> tokenAuthenticatorProvider;

  public NetworkModule_FournirOkHttpClientAuthentifieFactory(
      Provider<HttpLoggingInterceptor> loggingProvider,
      Provider<AuthInterceptor> authInterceptorProvider,
      Provider<TokenAuthenticator> tokenAuthenticatorProvider) {
    this.loggingProvider = loggingProvider;
    this.authInterceptorProvider = authInterceptorProvider;
    this.tokenAuthenticatorProvider = tokenAuthenticatorProvider;
  }

  @Override
  public OkHttpClient get() {
    return fournirOkHttpClientAuthentifie(loggingProvider.get(), authInterceptorProvider.get(), tokenAuthenticatorProvider.get());
  }

  public static NetworkModule_FournirOkHttpClientAuthentifieFactory create(
      Provider<HttpLoggingInterceptor> loggingProvider,
      Provider<AuthInterceptor> authInterceptorProvider,
      Provider<TokenAuthenticator> tokenAuthenticatorProvider) {
    return new NetworkModule_FournirOkHttpClientAuthentifieFactory(loggingProvider, authInterceptorProvider, tokenAuthenticatorProvider);
  }

  public static OkHttpClient fournirOkHttpClientAuthentifie(HttpLoggingInterceptor logging,
      AuthInterceptor authInterceptor, TokenAuthenticator tokenAuthenticator) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.fournirOkHttpClientAuthentifie(logging, authInterceptor, tokenAuthenticator));
  }
}
