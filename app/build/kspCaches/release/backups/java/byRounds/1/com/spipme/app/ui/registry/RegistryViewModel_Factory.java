package com.spipme.app.ui.registry;

import com.spipme.app.core.security.SessionManager;
import com.spipme.app.domain.repository.EntiteRepository;
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
public final class RegistryViewModel_Factory implements Factory<RegistryViewModel> {
  private final Provider<EntiteRepository> entiteRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public RegistryViewModel_Factory(Provider<EntiteRepository> entiteRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.entiteRepositoryProvider = entiteRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public RegistryViewModel get() {
    return newInstance(entiteRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static RegistryViewModel_Factory create(
      Provider<EntiteRepository> entiteRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new RegistryViewModel_Factory(entiteRepositoryProvider, sessionManagerProvider);
  }

  public static RegistryViewModel newInstance(EntiteRepository entiteRepository,
      SessionManager sessionManager) {
    return new RegistryViewModel(entiteRepository, sessionManager);
  }
}
