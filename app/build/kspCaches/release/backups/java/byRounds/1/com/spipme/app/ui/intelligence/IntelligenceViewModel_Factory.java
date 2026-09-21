package com.spipme.app.ui.intelligence;

import com.spipme.app.core.security.SessionManager;
import com.spipme.app.domain.repository.SuggestionRepository;
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
public final class IntelligenceViewModel_Factory implements Factory<IntelligenceViewModel> {
  private final Provider<SuggestionRepository> suggestionRepositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public IntelligenceViewModel_Factory(Provider<SuggestionRepository> suggestionRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.suggestionRepositoryProvider = suggestionRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public IntelligenceViewModel get() {
    return newInstance(suggestionRepositoryProvider.get(), sessionManagerProvider.get());
  }

  public static IntelligenceViewModel_Factory create(
      Provider<SuggestionRepository> suggestionRepositoryProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new IntelligenceViewModel_Factory(suggestionRepositoryProvider, sessionManagerProvider);
  }

  public static IntelligenceViewModel newInstance(SuggestionRepository suggestionRepository,
      SessionManager sessionManager) {
    return new IntelligenceViewModel(suggestionRepository, sessionManager);
  }
}
