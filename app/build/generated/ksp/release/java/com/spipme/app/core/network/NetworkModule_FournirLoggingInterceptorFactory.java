package com.spipme.app.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.logging.HttpLoggingInterceptor;

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
public final class NetworkModule_FournirLoggingInterceptorFactory implements Factory<HttpLoggingInterceptor> {
  @Override
  public HttpLoggingInterceptor get() {
    return fournirLoggingInterceptor();
  }

  public static NetworkModule_FournirLoggingInterceptorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HttpLoggingInterceptor fournirLoggingInterceptor() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.fournirLoggingInterceptor());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_FournirLoggingInterceptorFactory INSTANCE = new NetworkModule_FournirLoggingInterceptorFactory();
  }
}
