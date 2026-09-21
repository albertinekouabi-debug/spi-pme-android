package com.spipme.app.data.repository;

import com.spipme.app.data.remote.api.TacheApi;
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
public final class TacheRepositoryImpl_Factory implements Factory<TacheRepositoryImpl> {
  private final Provider<TacheApi> tacheApiProvider;

  private final Provider<Json> jsonProvider;

  public TacheRepositoryImpl_Factory(Provider<TacheApi> tacheApiProvider,
      Provider<Json> jsonProvider) {
    this.tacheApiProvider = tacheApiProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public TacheRepositoryImpl get() {
    return newInstance(tacheApiProvider.get(), jsonProvider.get());
  }

  public static TacheRepositoryImpl_Factory create(Provider<TacheApi> tacheApiProvider,
      Provider<Json> jsonProvider) {
    return new TacheRepositoryImpl_Factory(tacheApiProvider, jsonProvider);
  }

  public static TacheRepositoryImpl newInstance(TacheApi tacheApi, Json json) {
    return new TacheRepositoryImpl(tacheApi, json);
  }
}
