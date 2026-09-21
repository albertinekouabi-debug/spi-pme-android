package com.spipme.app;

import com.spipme.app.domain.repository.AuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AppViewModel_Factory implements Factory<AppViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public AppViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public AppViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static AppViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new AppViewModel_Factory(authRepositoryProvider);
  }

  public static AppViewModel newInstance(AuthRepository authRepository) {
    return new AppViewModel(authRepository);
  }
}
