package com.spipme.app.data.repository;

import com.spipme.app.data.remote.api.ImportApi;
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
public final class ImportRepositoryImpl_Factory implements Factory<ImportRepositoryImpl> {
  private final Provider<ImportApi> importApiProvider;

  private final Provider<Json> jsonProvider;

  public ImportRepositoryImpl_Factory(Provider<ImportApi> importApiProvider,
      Provider<Json> jsonProvider) {
    this.importApiProvider = importApiProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public ImportRepositoryImpl get() {
    return newInstance(importApiProvider.get(), jsonProvider.get());
  }

  public static ImportRepositoryImpl_Factory create(Provider<ImportApi> importApiProvider,
      Provider<Json> jsonProvider) {
    return new ImportRepositoryImpl_Factory(importApiProvider, jsonProvider);
  }

  public static ImportRepositoryImpl newInstance(ImportApi importApi, Json json) {
    return new ImportRepositoryImpl(importApi, json);
  }
}
