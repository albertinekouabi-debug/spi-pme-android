package com.spipme.app.di;

import com.spipme.app.data.local.AppDatabase;
import com.spipme.app.data.local.dao.UtilisateurLocalDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DatabaseModule_FournirUtilisateurLocalDaoFactory implements Factory<UtilisateurLocalDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_FournirUtilisateurLocalDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public UtilisateurLocalDao get() {
    return fournirUtilisateurLocalDao(dbProvider.get());
  }

  public static DatabaseModule_FournirUtilisateurLocalDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_FournirUtilisateurLocalDaoFactory(dbProvider);
  }

  public static UtilisateurLocalDao fournirUtilisateurLocalDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.fournirUtilisateurLocalDao(db));
  }
}
