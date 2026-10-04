package com.spipme.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2 : file de synchronisation hors ligne, correspondances d'ids locaux, cache de lecture.
 *
 * Migration EXPLICITE et additive (la table utilisateur_local est intouchée). `fallbackToDestructiveMigration`
 * a été retiré : il aurait effacé en silence des saisies non synchronisées à la première montée de schéma.
 * Le SQL reproduit exactement ce que Room génère pour les entités (types, NOT NULL, index) — Room le
 * revalide à l'ouverture de la base.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        MIGRATION_1_2_SQL.forEach(db::execSQL)
    }
}

val MIGRATION_1_2_SQL: List<String> = listOf(
    "CREATE TABLE IF NOT EXISTS `operation_en_attente` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
        "`idempotencyKey` TEXT NOT NULL, `type` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, " +
        "`methode` TEXT NOT NULL, `chemin` TEXT NOT NULL, `payloadJson` TEXT, `versionBase` INTEGER, " +
        "`refLocaleCreee` TEXT, `creeLe` INTEGER NOT NULL, `tentatives` INTEGER NOT NULL, " +
        "`derniereTentativeLe` INTEGER, `prochaineTentativeLe` INTEGER NOT NULL, `statut` TEXT NOT NULL, " +
        "`messageErreur` TEXT, `conflitJson` TEXT)",
    "CREATE UNIQUE INDEX IF NOT EXISTS `index_operation_en_attente_idempotencyKey` ON `operation_en_attente` (`idempotencyKey`)",
    "CREATE INDEX IF NOT EXISTS `index_operation_en_attente_statut` ON `operation_en_attente` (`statut`)",
    "CREATE TABLE IF NOT EXISTS `correspondance_id` (`referenceLocale` TEXT NOT NULL, `idServeur` TEXT NOT NULL, PRIMARY KEY(`referenceLocale`))",
    "CREATE TABLE IF NOT EXISTS `cache_reponse` (`cle` TEXT NOT NULL, `json` TEXT NOT NULL, `majLe` INTEGER NOT NULL, PRIMARY KEY(`cle`))",
)
