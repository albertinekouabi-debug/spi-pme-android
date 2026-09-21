package com.spipme.app.core.network;

import com.spipme.app.data.remote.api.SuggestionApi;
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
public final class NetworkModule_FournirSuggestionApiFactory implements Factory<SuggestionApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_FournirSuggestionApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public SuggestionApi get() {
    return fournirSuggestionApi(retrofitProvider.get());
  }

  public static NetworkModule_FournirSuggestionApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_FournirSuggestionApiFactory(retrofitProvider);
  }

  public static SuggestionApi fournirSuggestionApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.fournirSuggestionApi(retrofit));
  }
}
