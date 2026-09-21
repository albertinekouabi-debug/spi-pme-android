package com.spipme.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.spipme.app.data.local.entity.UtilisateurLocalEntity

@Dao
interface UtilisateurLocalDao {
    @Upsert
    suspend fun upsert(utilisateur: UtilisateurLocalEntity)

    @Query("SELECT * FROM utilisateur_local WHERE nomUtilisateur = :nomUtilisateur OR email = :nomUtilisateur LIMIT 1")
    suspend fun trouverParIdentifiant(nomUtilisateur: String): UtilisateurLocalEntity?

    @Query("DELETE FROM utilisateur_local")
    suspend fun toutSupprimer()
}
