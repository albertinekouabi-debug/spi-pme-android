package com.spipme.app.core.network;

import com.spipme.app.data.remote.api.AlerteApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
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
public final class NetworkModule_FournirAlerteApiFactory implements Factory<AlerteApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_FournirAlerteApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public AlerteApi get() {
    return fournirAlerteApi(retrofitProvider.get());
  }

  public static NetworkModule_FournirAlerteApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_FournirAlerteApiFactory(retrofitProvider);
  }

  public static AlerteApi fournirAlerteApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.fournirAlerteApi(retrofit));
  }
}
