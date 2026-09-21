package com.spipme.app.ui.alerts;

import com.spipme.app.core.security.SessionManager;
import com.spipme.app.domain.repository.AlerteRepository;
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
public final class AlertsViewModel_Factory implements Factory<AlertsViewModel> {
  private final Provider<AlerteRepository> alerteRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public AlertsViewModel_Factory(Provider<AlerteRepository> alerteRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.alerteRepositoryProvider = alerteRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public AlertsViewModel get() {
    return newInstance(alerteRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static AlertsViewModel_Factory create(Provider<AlerteRepository> alerteRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new AlertsViewModel_Factory(alerteRepositoryProvider, sessionManagerProvider);
  }

  public static AlertsViewModel newInstance(AlerteRepository alerteRepository,
      SessionManager sessionManager) {
    return new AlertsViewModel(alerteRepository, sessionManager);
  }
}
