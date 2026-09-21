package com.spipme.app.core.network;

import com.spipme.app.core.security.TokenManager;
import com.spipme.app.data.remote.api.AuthApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("com.spipme.app.core.network.ClientBrut")
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
public final class TokenAuthenticator_Factory implements Factory<TokenAuthenticator> {
  private final Provider<TokenManager> tokenManagerProvider;

  private final Provider<AuthApi> authApiBrutProvider;

  public TokenAuthenticator_Factory(Provider<TokenManager> tokenManagerProvider,
      Provider<AuthApi> authApiBrutProvider) {
    this.tokenManagerProvider = tokenManagerProvider;
    this.authApiBrutProvider = authApiBrutProvider;
  }

  @Override
  public TokenAuthenticator get() {
    return newInstance(tokenManagerProvider.get(), authApiBrutProvider.get());
  }

  public static TokenAuthenticator_Factory create(Provider<TokenManager> tokenManagerProvider,
      Provider<AuthApi> authApiBrutProvider) {
    return new TokenAuthenticator_Factory(tokenManagerProvider, authApiBrutProvider);
  }

  public static TokenAuthenticator newInstance(TokenManager tokenManager, AuthApi authApiBrut) {
    return new TokenAuthenticator(tokenManager, authApiBrut);
  }
}
