package com.spipme.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spipme.app.data.local.entity.CacheReponseEntity
import com.spipme.app.data.local.entity.CorrespondanceIdEntity
import com.spipme.app.data.local.entity.OperationEnAttenteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationDao {
    /** ABORT : une même clé d'idempotence ne peut exister qu'une fois (jamais deux fois la même saisie). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserer(operation: OperationEnAttenteEntity): Long

    @Update
    suspend fun mettreAJour(operation: OperationEnAttenteEntity)

    @Query("SELECT * FROM operation_en_attente WHERE utilisateurId = :utilisateurId AND statut = 'EN_ATTENTE' ORDER BY creeLe ASC, id ASC")
    suspend fun aEnvoyer(utilisateurId: Long): List<OperationEnAttenteEntity>

    @Query("SELECT * FROM operation_en_attente WHERE id = :id")
    suspend fun parId(id: Long): OperationEnAttenteEntity?

    @Query("DELETE FROM operation_en_attente WHERE id = :id")
    suspend fun supprimer(id: Long)

    @Query("UPDATE operation_en_attente SET statut = 'EN_ATTENTE' WHERE statut = 'EN_COURS'")
    suspend fun recupererOrphelines()

    @Query("UPDATE operation_en_attente SET statut = 'EN_ATTENTE', tentatives = 0, prochaineTentativeLe = 0, messageErreur = NULL WHERE id = :id AND statut = 'ECHEC_DEFINITIF'")
    suspend fun relancer(id: Long)

    @Query("SELECT COUNT(*) FROM operation_en_attente WHERE utilisateurId = :utilisateurId AND statut IN ('EN_ATTENTE', 'EN_COURS')")
    fun compterEnAttente(utilisateurId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM operation_en_attente WHERE utilisateurId = :utilisateurId AND statut = 'CONFLIT'")
    fun compterConflits(utilisateurId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM operation_en_attente WHERE utilisateurId = :utilisateurId AND statut = 'ECHEC_DEFINITIF'")
    fun compterEchecs(utilisateurId: Long): Flow<Int>

    @Query("SELECT * FROM operation_en_attente WHERE utilisateurId = :utilisateurId AND statut IN ('CONFLIT', 'ECHEC_DEFINITIF') ORDER BY creeLe ASC")
    fun aTraiter(utilisateurId: Long): Flow<List<OperationEnAttenteEntity>>

    /** Saisies hors ligne pas encore synchronisées, pour les superposer aux listes (« en attente »). */
    @Query("SELECT * FROM operation_en_attente WHERE utilisateurId = :utilisateurId AND type IN (:types) AND statut IN ('EN_ATTENTE', 'EN_COURS') ORDER BY creeLe ASC")
    fun enAttentePourTypes(utilisateurId: Long, types: List<String>): Flow<List<OperationEnAttenteEntity>>

    /** Entités d'un type ayant une écriture en attente (superposition « en attente de synchronisation »). */
    @Query("SELECT entityId FROM operation_en_attente WHERE utilisateurId = :utilisateurId AND entityType = :entityType AND statut IN ('EN_ATTENTE', 'EN_COURS')")
    fun entitesEnAttente(utilisateurId: Long, entityType: String): Flow<List<String>>
}

@Dao
interface CorrespondanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enregistrer(correspondance: CorrespondanceIdEntity)

    @Query("SELECT idServeur FROM correspondance_id WHERE referenceLocale = :reference")
    suspend fun resoudre(reference: String): String?
}

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enregistrer(entree: CacheReponseEntity)

    @Query("SELECT * FROM cache_reponse WHERE cle = :cle")
    suspend fun lire(cle: String): CacheReponseEntity?

    /** Déconnexion : les lectures mémorisées d'un compte ne doivent pas rester lisibles pour le suivant. */
    @Query("DELETE FROM cache_reponse")
    suspend fun supprimerTout()
}
