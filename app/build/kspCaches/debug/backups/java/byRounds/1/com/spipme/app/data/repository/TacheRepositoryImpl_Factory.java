package com.spipme.app.data.repository;

import com.spipme.app.core.sync.android.EcritureHorsLigne;
import com.spipme.app.data.local.CacheLecture;
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
  private final Provider<EcritureHorsLigne> ecritureProvider;

  private final Provider<CacheLecture> cacheLectureProvider;

  private final Provider<TacheApi> tacheApiProvider;

  private final Provider<Json> jsonProvider;

  public TacheRepositoryImpl_Factory(Provider<EcritureHorsLigne> ecritureProvider,
      Provider<CacheLecture> cacheLectureProvider, Provider<TacheApi> tacheApiProvider,
      Provider<Json> jsonProvider) {
    this.ecritureProvider = ecritureProvider;
    this.cacheLectureProvider = cacheLectureProvider;
    this.tacheApiProvider = tacheApiProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public TacheRepositoryImpl get() {
    return newInstance(ecritureProvider.get(), cacheLectureProvider.get(), tacheApiProvider.get(), jsonProvider.get());
  }

  public static TacheRepositoryImpl_Factory create(Provider<EcritureHorsLigne> ecritureProvider,
      Provider<CacheLecture> cacheLectureProvider, Provider<TacheApi> tacheApiProvider,
      Provider<Json> jsonProvider) {
    return new TacheRepositoryImpl_Factory(ecritureProvider, cacheLectureProvider, tacheApiProvider, jsonProvider);
  }

  public static TacheRepositoryImpl newInstance(EcritureHorsLigne ecriture,
      CacheLecture cacheLecture, TacheApi tacheApi, Json json) {
    return new TacheRepositoryImpl(ecriture, cacheLecture, tacheApi, json);
  }
}
