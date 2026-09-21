package com.spipme.app.data.repository;

import com.spipme.app.data.remote.api.EntiteApi;
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
public final class EntiteRepositoryImpl_Factory implements Factory<EntiteRepositoryImpl> {
  private final Provider<EntiteApi> entiteApiProvider;

  private final Provider<Json> jsonProvider;

  public EntiteRepositoryImpl_Factory(Provider<EntiteApi> entiteApiProvider,
      Provider<Json> jsonProvider) {
    this.entiteApiProvider = entiteApiProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public EntiteRepositoryImpl get() {
    return newInstance(entiteApiProvider.get(), jsonProvider.get());
  }

  public static EntiteRepositoryImpl_Factory create(Provider<EntiteApi> entiteApiProvider,
      Provider<Json> jsonProvider) {
    return new EntiteRepositoryImpl_Factory(entiteApiProvider, jsonProvider);
  }

  public static EntiteRepositoryImpl newInstance(EntiteApi entiteApi, Json json) {
    return new EntiteRepositoryImpl(entiteApi, json);
  }
}
