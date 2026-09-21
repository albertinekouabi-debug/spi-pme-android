package com.spipme.app.data.repository;

import com.spipme.app.data.remote.api.RessourceApi;
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
public final class RessourceRepositoryImpl_Factory implements Factory<RessourceRepositoryImpl> {
  private final Provider<RessourceApi> ressourceApiProvider;

  private final Provider<Json> jsonProvider;

  public RessourceRepositoryImpl_Factory(Provider<RessourceApi> ressourceApiProvider,
      Provider<Json> jsonProvider) {
    this.ressourceApiProvider = ressourceApiProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public RessourceRepositoryImpl get() {
    return newInstance(ressourceApiProvider.get(), jsonProvider.get());
  }

  public static RessourceRepositoryImpl_Factory create(Provider<RessourceApi> ressourceApiProvider,
      Provider<Json> jsonProvider) {
    return new RessourceRepositoryImpl_Factory(ressourceApiProvider, jsonProvider);
  }

  public static RessourceRepositoryImpl newInstance(RessourceApi ressourceApi, Json json) {
    return new RessourceRepositoryImpl(ressourceApi, json);
  }
}
