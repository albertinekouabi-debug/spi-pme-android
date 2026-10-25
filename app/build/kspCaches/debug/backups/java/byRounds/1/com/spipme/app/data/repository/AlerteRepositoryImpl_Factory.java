package com.spipme.app.data.repository;

import com.spipme.app.data.local.CacheLecture;
import com.spipme.app.data.remote.api.AlerteApi;
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
public final class AlerteRepositoryImpl_Factory implements Factory<AlerteRepositoryImpl> {
  private final Provider<CacheLecture> cacheLectureProvider;

  private final Provider<AlerteApi> alerteApiProvider;

  private final Provider<Json> jsonProvider;

  public AlerteRepositoryImpl_Factory(Provider<CacheLecture> cacheLectureProvider,
      Provider<AlerteApi> alerteApiProvider, Provider<Json> jsonProvider) {
    this.cacheLectureProvider = cacheLectureProvider;
    this.alerteApiProvider = alerteApiProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public AlerteRepositoryImpl get() {
    return newInstance(cacheLectureProvider.get(), alerteApiProvider.get(), jsonProvider.get());
  }

  public static AlerteRepositoryImpl_Factory create(Provider<CacheLecture> cacheLectureProvider,
      Provider<AlerteApi> alerteApiProvider, Provider<Json> jsonProvider) {
    return new AlerteRepositoryImpl_Factory(cacheLectureProvider, alerteApiProvider, jsonProvider);
  }

  public static AlerteRepositoryImpl newInstance(CacheLecture cacheLecture, AlerteApi alerteApi,
      Json json) {
    return new AlerteRepositoryImpl(cacheLecture, alerteApi, json);
  }
}
