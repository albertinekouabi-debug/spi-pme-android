package com.spipme.app.core.network;

import com.spipme.app.data.remote.api.AuthApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NetworkModule_FournirAuthApiBrutFactory implements Factory<AuthApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_FournirAuthApiBrutFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public AuthApi get() {
    return fournirAuthApiBrut(retrofitProvider.get());
  }

  public static NetworkModule_FournirAuthApiBrutFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_FournirAuthApiBrutFactory(retrofitProvider);
  }

  public static AuthApi fournirAuthApiBrut(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.fournirAuthApiBrut(retrofit));
  }
}
