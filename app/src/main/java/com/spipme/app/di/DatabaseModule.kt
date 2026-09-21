package com.spipme.app.di

import android.content.Context
import androidx.room.Room
import com.spipme.app.data.local.AppDatabase
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
            .fallbackToDestructiveMigration() // acceptable en v1, à retirer dès la première migration réelle en production
            .build()

    @Provides
    @Singleton
    fun fournirUtilisateurLocalDao(db: AppDatabase): UtilisateurLocalDao = db.utilisateurLocalDao()
}
