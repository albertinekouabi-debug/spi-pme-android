package com.spipme.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** File persistante des écritures hors ligne (miroir de core.sync.OperationEnAttente). */
@Entity(
    tableName = "operation_en_attente",
    indices = [Index(value = ["idempotencyKey"], unique = true), Index(value = ["statut"])],
)
data class OperationEnAttenteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idempotencyKey: String,
    val type: String,
    val entityType: String,
    val entityId: String,
    val methode: String,
    val chemin: String,
    val payloadJson: String?,
    val versionBase: Int?,
    val refLocaleCreee: String?,
    val creeLe: Long,
    val tentatives: Int,
    val derniereTentativeLe: Long?,
    val prochaineTentativeLe: Long,
    val statut: String,
    val messageErreur: String?,
    val conflitJson: String?,
)

/** `@local:<uuid>` → id serveur, conservé pour les opérations qui se référencent entre elles. */
@Entity(tableName = "correspondance_id")
data class CorrespondanceIdEntity(
    @PrimaryKey val referenceLocale: String,
    val idServeur: String,
)

/** Dernière réponse réussie d'une liste (lecture hors ligne). `cle` = identifie endpoint + filtres. */
@Entity(tableName = "cache_reponse")
data class CacheReponseEntity(
    @PrimaryKey val cle: String,
    val json: String,
    val majLe: Long,
)
