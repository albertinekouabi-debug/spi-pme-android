package com.spipme.app.di

import com.spipme.app.data.repository.AlerteRepositoryImpl
import com.spipme.app.data.repository.AuditRepositoryImpl
import com.spipme.app.data.repository.ConformiteRepositoryImpl
import com.spipme.app.data.repository.AdminRepositoryImpl
import com.spipme.app.data.repository.MoiRepositoryImpl
import com.spipme.app.data.repository.SecteurRepositoryImpl
import com.spipme.app.data.repository.FactureRepositoryImpl
import com.spipme.app.data.repository.AuthRepositoryImpl
import com.spipme.app.data.repository.EntiteRepositoryImpl
import com.spipme.app.data.repository.ImportRepositoryImpl
import com.spipme.app.data.repository.RessourceRepositoryImpl
import com.spipme.app.data.repository.SuggestionRepositoryImpl
import com.spipme.app.data.repository.TacheRepositoryImpl
import com.spipme.app.data.repository.TransactionRepositoryImpl
import com.spipme.app.domain.repository.AlerteRepository
import com.spipme.app.domain.repository.AuditRepository
import com.spipme.app.domain.repository.ConformiteRepository
import com.spipme.app.domain.repository.AdminRepository
import com.spipme.app.domain.repository.MoiRepository
import com.spipme.app.domain.repository.SecteurRepository
import com.spipme.app.domain.repository.FactureRepository
import com.spipme.app.domain.repository.AuthRepository
import com.spipme.app.domain.repository.EntiteRepository
import com.spipme.app.domain.repository.ImportRepository
import com.spipme.app.domain.repository.RessourceRepository
import com.spipme.app.domain.repository.SuggestionRepository
import com.spipme.app.domain.repository.TacheRepository
import com.spipme.app.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun lierAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun lierEntiteRepository(impl: EntiteRepositoryImpl): EntiteRepository

    @Binds
    @Singleton
    abstract fun lierRessourceRepository(impl: RessourceRepositoryImpl): RessourceRepository

    @Binds
    @Singleton
    abstract fun lierTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun lierTacheRepository(impl: TacheRepositoryImpl): TacheRepository

    @Binds
    @Singleton
    abstract fun lierSuggestionRepository(impl: SuggestionRepositoryImpl): SuggestionRepository

    @Binds
    @Singleton
    abstract fun lierAlerteRepository(impl: AlerteRepositoryImpl): AlerteRepository

    @Binds
    @Singleton
    abstract fun lierImportRepository(impl: ImportRepositoryImpl): ImportRepository

    @Binds
    @Singleton
    abstract fun lierAuditRepository(impl: AuditRepositoryImpl): AuditRepository

    @Binds
    @Singleton
    abstract fun lierFactureRepository(impl: FactureRepositoryImpl): FactureRepository

    @Binds
    @Singleton
    abstract fun lierConformiteRepository(impl: ConformiteRepositoryImpl): ConformiteRepository

    @Binds
    @Singleton
    abstract fun lierSecteurRepository(impl: SecteurRepositoryImpl): SecteurRepository

    @Binds
    @Singleton
    abstract fun lierMoiRepository(impl: MoiRepositoryImpl): MoiRepository

    @Binds
    @Singleton
    abstract fun lierAdminRepository(impl: AdminRepositoryImpl): AdminRepository
}

