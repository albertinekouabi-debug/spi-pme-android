package com.spipme.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spipme.app.data.local.dao.UtilisateurLocalDao
import com.spipme.app.data.local.entity.UtilisateurLocalEntity

/**
 * Version 1 : cache utilisateur pour la connexion hors ligne uniquement.
 * Les entités du mode hors ligne complet (Ressource, Tache, file de
 * synchronisation...) seront ajoutées avec le module Offline dédié, pour
 * rester dans le périmètre du module en cours (Identité & Accès).
 */
@Database(
    entities = [UtilisateurLocalEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun utilisateurLocalDao(): UtilisateurLocalDao
}
