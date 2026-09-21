package com.spipme.app.ui.tasks.creation;

import com.spipme.app.core.security.SessionManager;
import com.spipme.app.domain.repository.TacheRepository;
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
public final class CreerTacheViewModel_Factory implements Factory<CreerTacheViewModel> {
  private final Provider<TacheRepository> tacheRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public CreerTacheViewModel_Factory(Provider<TacheRepository> tacheRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.tacheRepositoryProvider = tacheRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public CreerTacheViewModel get() {
    return newInstance(tacheRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static CreerTacheViewModel_Factory create(
      Provider<TacheRepository> tacheRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new CreerTacheViewModel_Factory(tacheRepositoryProvider, sessionManagerProvider);
  }

  public static CreerTacheViewModel newInstance(TacheRepository tacheRepository,
      SessionManager sessionManager) {
    return new CreerTacheViewModel(tacheRepository, sessionManager);
  }
}
