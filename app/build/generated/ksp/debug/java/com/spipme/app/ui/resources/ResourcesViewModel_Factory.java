package com.spipme.app.ui.resources;

import com.spipme.app.core.security.SessionManager;
import com.spipme.app.domain.repository.RessourceRepository;
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
public final class ResourcesViewModel_Factory implements Factory<ResourcesViewModel> {
  private final Provider<RessourceRepository> ressourceRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public ResourcesViewModel_Factory(Provider<RessourceRepository> ressourceRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.ressourceRepositoryProvider = ressourceRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public ResourcesViewModel get() {
    return newInstance(ressourceRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static ResourcesViewModel_Factory create(
      Provider<RessourceRepository> ressourceRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new ResourcesViewModel_Factory(ressourceRepositoryProvider, sessionManagerProvider);
  }

  public static ResourcesViewModel newInstance(RessourceRepository ressourceRepository,
      SessionManager sessionManager) {
    return new ResourcesViewModel(ressourceRepository, sessionManager);
  }
}
