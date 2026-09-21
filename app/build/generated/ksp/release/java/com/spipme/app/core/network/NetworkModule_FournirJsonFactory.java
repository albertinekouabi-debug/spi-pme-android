package com.spipme.app.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
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
public final class NetworkModule_FournirJsonFactory implements Factory<Json> {
  @Override
  public Json get() {
    return fournirJson();
  }

  public static NetworkModule_FournirJsonFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Json fournirJson() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.fournirJson());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_FournirJsonFactory INSTANCE = new NetworkModule_FournirJsonFactory();
  }
}
