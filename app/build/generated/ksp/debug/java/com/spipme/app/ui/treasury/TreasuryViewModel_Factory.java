package com.spipme.app.ui.treasury;

import com.spipme.app.core.security.SessionManager;
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
public final class TreasuryViewModel_Factory implements Factory<TreasuryViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public TreasuryViewModel_Factory(Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public TreasuryViewModel get() {
    return newInstance(transactionRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static TreasuryViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new TreasuryViewModel_Factory(transactionRepositoryProvider, sessionManagerProvider);
  }

  public static TreasuryViewModel newInstance(TransactionRepository transactionRepository,
      SessionManager sessionManager) {
    return new TreasuryViewModel(transactionRepository, sessionManager);
  }
}
