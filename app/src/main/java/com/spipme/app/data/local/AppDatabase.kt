package com.spipme.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spipme.app.data.local.dao.CacheDao
import com.spipme.app.data.local.dao.CorrespondanceDao
import com.spipme.app.data.local.dao.OperationDao
import com.spipme.app.data.local.dao.UtilisateurLocalDao
import com.spipme.app.data.local.entity.CacheReponseEntity
import com.spipme.app.data.local.entity.CorrespondanceIdEntity
import com.spipme.app.data.local.entity.OperationEnAttenteEntity
import com.spipme.app.data.local.entity.UtilisateurLocalEntity

/**
 * v1 : cache utilisateur (connexion hors ligne).
 * v2 : file d'opérations à synchroniser, correspondances d'ids locaux, cache de lecture hors ligne
 *      (voir MIGRATION_1_2 — migration explicite, aucune réinitialisation destructive).
 */
@Database(
    entities = [
        UtilisateurLocalEntity::class,
        OperationEnAttenteEntity::class,
        CorrespondanceIdEntity::class,
        CacheReponseEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun utilisateurLocalDao(): UtilisateurLocalDao
    abstract fun operationDao(): OperationDao
    abstract fun correspondanceDao(): CorrespondanceDao
    abstract fun cacheDao(): CacheDao
}
