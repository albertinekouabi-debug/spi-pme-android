package com.spipme.app.ui.auth.offline;

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
public final class OfflineLoginViewModel_Factory implements Factory<OfflineLoginViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public OfflineLoginViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public OfflineLoginViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static OfflineLoginViewModel_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new OfflineLoginViewModel_Factory(authRepositoryProvider);
  }

  public static OfflineLoginViewModel newInstance(AuthRepository authRepository) {
    return new OfflineLoginViewModel(authRepository);
  }
}
