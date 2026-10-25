package com.spipme.app.data.repository;

import com.spipme.app.core.sync.android.EcritureHorsLigne;
import com.spipme.app.data.local.CacheLecture;
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
  private final Provider<EcritureHorsLigne> ecritureProvider;

  private final Provider<CacheLecture> cacheLectureProvider;

  private final Provider<EntiteApi> entiteApiProvider;

  private final Provider<Json> jsonProvider;

  public EntiteRepositoryImpl_Factory(Provider<EcritureHorsLigne> ecritureProvider,
      Provider<CacheLecture> cacheLectureProvider, Provider<EntiteApi> entiteApiProvider,
      Provider<Json> jsonProvider) {
    this.ecritureProvider = ecritureProvider;
    this.cacheLectureProvider = cacheLectureProvider;
    this.entiteApiProvider = entiteApiProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public EntiteRepositoryImpl get() {
    return newInstance(ecritureProvider.get(), cacheLectureProvider.get(), entiteApiProvider.get(), jsonProvider.get());
  }

  public static EntiteRepositoryImpl_Factory create(Provider<EcritureHorsLigne> ecritureProvider,
      Provider<CacheLecture> cacheLectureProvider, Provider<EntiteApi> entiteApiProvider,
      Provider<Json> jsonProvider) {
    return new EntiteRepositoryImpl_Factory(ecritureProvider, cacheLectureProvider, entiteApiProvider, jsonProvider);
  }

  public static EntiteRepositoryImpl newInstance(EcritureHorsLigne ecriture,
      CacheLecture cacheLecture, EntiteApi entiteApi, Json json) {
    return new EntiteRepositoryImpl(ecriture, cacheLecture, entiteApi, json);
  }
}
