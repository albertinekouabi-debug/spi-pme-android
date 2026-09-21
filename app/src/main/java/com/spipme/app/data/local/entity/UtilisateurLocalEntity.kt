package com.spipme.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cache local minimal, créé/mis à jour après chaque connexion EN LIGNE
 * réussie — permet ensuite la "Connexion hors ligne" de la maquette sans
 * jamais transmettre le mot de passe en clair (hashPbkdf2 comparé
 * localement, cf. core.security.HachageMotDePasse).
 */
@Entity(tableName = "utilisateur_local")
data class UtilisateurLocalEntity(
    @PrimaryKey val id: Int,
    val nomUtilisateur: String,
    val email: String,
    val nomComplet: String?,
    val hashMotDePasse: String,
    val roleNom: String?,
    val secteurPrincipalId: Int?,
    val secteurPrincipalNom: String?,
    val derniereSynchronisation: Long,
)
