package com.spipme.app.domain.model

data class Anomalie(val ligne: Int, val colonne: String, val motif: String)

data class ImportFichier(
    val id: Int,
    val nomFichier: String,
    val typeFichier: String,
    val statut: String,
    val lignesTotales: Int,
    val lignesImportees: Int,
    val lignesRejetees: Int,
    val rapportAnomalies: List<Anomalie>,
    val auteurNom: String?,
    val secteurId: Int,
    val dateImport: String,
)

data class ApercuImport(
    val lignesTotales: Int,
    val lignesValides: Int,
    val lignesRejetees: Int,
    val anomalies: List<Anomalie>,
    /** Chaque ligne de l'aperçu, générique (clé/valeur) — le format de colonnes dépend du fichier importé. */
    val apercu: List<List<Pair<String, String>>>,
)

/** Fichier sélectionné localement, avant tout appel réseau. */
data class FichierSelectionne(val nom: String, val octets: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FichierSelectionne) return false
        return nom == other.nom && octets.contentEquals(other.octets)
    }

    override fun hashCode(): Int = 31 * nom.hashCode() + octets.contentHashCode()
}
