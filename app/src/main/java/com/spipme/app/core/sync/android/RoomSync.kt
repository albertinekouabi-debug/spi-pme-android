package com.spipme.app.core.sync.android

import com.spipme.app.core.sync.CorrespondancesIds
import com.spipme.app.core.sync.FileOperations
import com.spipme.app.core.sync.OperationEnAttente
import com.spipme.app.core.sync.StatutOperation
import com.spipme.app.data.local.dao.CorrespondanceDao
import com.spipme.app.data.local.dao.OperationDao
import com.spipme.app.data.local.entity.CorrespondanceIdEntity
import com.spipme.app.data.local.entity.OperationEnAttenteEntity
import javax.inject.Inject
import javax.inject.Singleton

internal fun OperationEnAttente.versEntite() = OperationEnAttenteEntity(
    id = id, idempotencyKey = idempotencyKey, type = type, entityType = entityType, entityId = entityId,
    methode = methode, chemin = chemin, payloadJson = payloadJson, versionBase = versionBase,
    refLocaleCreee = refLocaleCreee, creeLe = creeLe, tentatives = tentatives,
    derniereTentativeLe = derniereTentativeLe, prochaineTentativeLe = prochaineTentativeLe,
    statut = statut.name, messageErreur = messageErreur, conflitJson = conflitJson,
)

internal fun OperationEnAttenteEntity.versDomaine() = OperationEnAttente(
    id = id, idempotencyKey = idempotencyKey, type = type, entityType = entityType, entityId = entityId,
    methode = methode, chemin = chemin, payloadJson = payloadJson, versionBase = versionBase,
    refLocaleCreee = refLocaleCreee, creeLe = creeLe, tentatives = tentatives,
    derniereTentativeLe = derniereTentativeLe, prochaineTentativeLe = prochaineTentativeLe,
    // Un statut inconnu (base d'une version future) est traité comme « en attente » plutôt que de planter.
    statut = runCatching { StatutOperation.valueOf(statut) }.getOrDefault(StatutOperation.EN_ATTENTE),
    messageErreur = messageErreur, conflitJson = conflitJson,
)

@Singleton
class RoomFileOperations @Inject constructor(private val dao: OperationDao) : FileOperations {
    override suspend fun operationsAEnvoyer() = dao.aEnvoyer().map { it.versDomaine() }
    override suspend fun mettreAJour(operation: OperationEnAttente) = dao.mettreAJour(operation.versEntite())
    override suspend fun supprimer(id: Long) = dao.supprimer(id)
    override suspend fun recupererOrphelines() = dao.recupererOrphelines()
}

@Singleton
class RoomCorrespondances @Inject constructor(private val dao: CorrespondanceDao) : CorrespondancesIds {
    override suspend fun resoudre(referenceLocale: String) = dao.resoudre(referenceLocale)
    override suspend fun enregistrer(referenceLocale: String, idServeur: String) =
        dao.enregistrer(CorrespondanceIdEntity(referenceLocale, idServeur))
}
