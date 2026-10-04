package com.spipme.app.di

import android.content.Context
import androidx.room.Room
import com.spipme.app.data.local.AppDatabase
import com.spipme.app.data.local.MIGRATION_1_2
import com.spipme.app.data.local.dao.CacheDao
import com.spipme.app.data.local.dao.CorrespondanceDao
import com.spipme.app.data.local.dao.OperationDao
import com.spipme.app.data.local.dao.UtilisateurLocalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun fournirAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "spi_pme.db")
            // Jamais de fallbackToDestructiveMigration : la base contient des saisies hors ligne pas
            // encore synchronisées. Toute évolution de schéma exige une migration explicite.
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun fournirUtilisateurLocalDao(db: AppDatabase): UtilisateurLocalDao = db.utilisateurLocalDao()

    @Provides
    fun fournirOperationDao(db: AppDatabase): OperationDao = db.operationDao()

    @Provides
    fun fournirCorrespondanceDao(db: AppDatabase): CorrespondanceDao = db.correspondanceDao()

    @Provides
    fun fournirCacheDao(db: AppDatabase): CacheDao = db.cacheDao()
}
