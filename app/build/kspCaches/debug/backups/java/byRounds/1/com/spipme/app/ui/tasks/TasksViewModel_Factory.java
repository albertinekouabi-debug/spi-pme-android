package com.spipme.app.ui.tasks;

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
public final class TasksViewModel_Factory implements Factory<TasksViewModel> {
  private final Provider<TacheRepository> tacheRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public TasksViewModel_Factory(Provider<TacheRepository> tacheRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.tacheRepositoryProvider = tacheRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public TasksViewModel get() {
    return newInstance(tacheRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static TasksViewModel_Factory create(Provider<TacheRepository> tacheRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new TasksViewModel_Factory(tacheRepositoryProvider, sessionManagerProvider);
  }

  public static TasksViewModel newInstance(TacheRepository tacheRepository,
      SessionManager sessionManager) {
    return new TasksViewModel(tacheRepository, sessionManager);
  }
}
