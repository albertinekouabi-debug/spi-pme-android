package com.spipme.app.data.repository;

import com.spipme.app.data.remote.api.SuggestionApi;
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
public final class SuggestionRepositoryImpl_Factory implements Factory<SuggestionRepositoryImpl> {
  private final Provider<SuggestionApi> suggestionApiProvider;

  private final Provider<Json> jsonProvider;

  public SuggestionRepositoryImpl_Factory(Provider<SuggestionApi> suggestionApiProvider,
      Provider<Json> jsonProvider) {
    this.suggestionApiProvider = suggestionApiProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public SuggestionRepositoryImpl get() {
    return newInstance(suggestionApiProvider.get(), jsonProvider.get());
  }

  public static SuggestionRepositoryImpl_Factory create(
      Provider<SuggestionApi> suggestionApiProvider, Provider<Json> jsonProvider) {
    return new SuggestionRepositoryImpl_Factory(suggestionApiProvider, jsonProvider);
  }

  public static SuggestionRepositoryImpl newInstance(SuggestionApi suggestionApi, Json json) {
    return new SuggestionRepositoryImpl(suggestionApi, json);
  }
}
