package com.spipme.app.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class NetworkModule_FournirRetrofitAuthentifieFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> clientProvider;

  private final Provider<Json> jsonProvider;

  public NetworkModule_FournirRetrofitAuthentifieFactory(Provider<OkHttpClient> clientProvider,
      Provider<Json> jsonProvider) {
    this.clientProvider = clientProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public Retrofit get() {
    return fournirRetrofitAuthentifie(clientProvider.get(), jsonProvider.get());
  }

  public static NetworkModule_FournirRetrofitAuthentifieFactory create(
      Provider<OkHttpClient> clientProvider, Provider<Json> jsonProvider) {
    return new NetworkModule_FournirRetrofitAuthentifieFactory(clientProvider, jsonProvider);
  }

  public static Retrofit fournirRetrofitAuthentifie(OkHttpClient client, Json json) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.fournirRetrofitAuthentifie(client, json));
  }
}
