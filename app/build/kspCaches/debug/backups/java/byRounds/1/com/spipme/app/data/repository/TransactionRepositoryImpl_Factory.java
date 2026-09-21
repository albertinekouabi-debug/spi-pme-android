package com.spipme.app.data.repository;

import com.spipme.app.data.remote.api.TransactionApi;
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
public final class TransactionRepositoryImpl_Factory implements Factory<TransactionRepositoryImpl> {
  private final Provider<TransactionApi> transactionApiProvider;

  private final Provider<Json> jsonProvider;

  public TransactionRepositoryImpl_Factory(Provider<TransactionApi> transactionApiProvider,
      Provider<Json> jsonProvider) {
    this.transactionApiProvider = transactionApiProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public TransactionRepositoryImpl get() {
    return newInstance(transactionApiProvider.get(), jsonProvider.get());
  }

  public static TransactionRepositoryImpl_Factory create(
      Provider<TransactionApi> transactionApiProvider, Provider<Json> jsonProvider) {
    return new TransactionRepositoryImpl_Factory(transactionApiProvider, jsonProvider);
  }

  public static TransactionRepositoryImpl newInstance(TransactionApi transactionApi, Json json) {
    return new TransactionRepositoryImpl(transactionApi, json);
  }
}
