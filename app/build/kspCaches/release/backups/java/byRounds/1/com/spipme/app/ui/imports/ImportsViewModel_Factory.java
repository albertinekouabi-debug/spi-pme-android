package com.spipme.app.ui.imports;

import com.spipme.app.core.security.SessionManager;
import com.spipme.app.domain.repository.ImportRepository;
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
public final class ImportsViewModel_Factory implements Factory<ImportsViewModel> {
  private final Provider<ImportRepository> importRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public ImportsViewModel_Factory(Provider<ImportRepository> importRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.importRepositoryProvider = importRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public ImportsViewModel get() {
    return newInstance(importRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static ImportsViewModel_Factory create(Provider<ImportRepository> importRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new ImportsViewModel_Factory(importRepositoryProvider, sessionManagerProvider);
  }

  public static ImportsViewModel newInstance(ImportRepository importRepository,
      SessionManager sessionManager) {
    return new ImportsViewModel(importRepository, sessionManager);
  }
}
