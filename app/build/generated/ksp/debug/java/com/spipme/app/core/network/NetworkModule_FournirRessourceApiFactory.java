package com.spipme.app.core.network;

import com.spipme.app.data.remote.api.RessourceApi;
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
public final class NetworkModule_FournirRessourceApiFactory implements Factory<RessourceApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_FournirRessourceApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public RessourceApi get() {
    return fournirRessourceApi(retrofitProvider.get());
  }

  public static NetworkModule_FournirRessourceApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_FournirRessourceApiFactory(retrofitProvider);
  }

  public static RessourceApi fournirRessourceApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.fournirRessourceApi(retrofit));
  }
}
