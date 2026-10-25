package com.spipme.app.ui.resources;

import com.spipme.app.core.security.SessionManager;
import com.spipme.app.core.sync.android.SynchronisationRepository;
import com.spipme.app.domain.repository.RessourceRepository;
import com.spipme.app.domain.repository.TransactionRepository;
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

  private final Provider<SynchronisationRepository> synchronisationProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public ResourcesViewModel_Factory(Provider<RessourceRepository> ressourceRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<SynchronisationRepository> synchronisationProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    this.ressourceRepositoryProvider = ressourceRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.synchronisationProvider = synchronisationProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public ResourcesViewModel get() {
    return newInstance(ressourceRepositoryProvider.get(), sessionManagerProvider.get(), synchronisationProvider.get(), transactionRepositoryProvider.get());
  }

  public static ResourcesViewModel_Factory create(
      Provider<RessourceRepository> ressourceRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<SynchronisationRepository> synchronisationProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new ResourcesViewModel_Factory(ressourceRepositoryProvider, sessionManagerProvider, synchronisationProvider, transactionRepositoryProvider);
  }

  public static ResourcesViewModel newInstance(RessourceRepository ressourceRepository,
      SessionManager sessionManager, SynchronisationRepository synchronisation,
      TransactionRepository transactionRepository) {
    return new ResourcesViewModel(ressourceRepository, sessionManager, synchronisation, transactionRepository);
  }
}
