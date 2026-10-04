package com.spipme.app.core.sync

/** Résultat d'UNE tentative d'envoi, déjà classé par la passerelle réseau selon le code HTTP. */
sealed interface ResultatEnvoi {
    /** 2xx (y compris un rejeu idempotent). `idServeur` : id de l'entité créée, s'il y en a un. */
    data class Succes(val idServeur: String? = null) : ResultatEnvoi

    /** Pas de réseau / timeout / connexion coupée : réessayer plus tard, sans compter d'échec. */
    data class Reseau(val message: String) : ResultatEnvoi

    /** 5xx, 429, 408 : le serveur est momentanément indisponible. */
    data class ServeurTemporaire(val code: Int, val message: String) : ResultatEnvoi

    /** 4xx métier/validation (400, 403, 404, 409, 422) : rejouer ne changera rien. */
    data class Definitive(val code: Int, val message: String) : ResultatEnvoi

    /** 401 subsistant après tentative de rafraîchissement du jeton : session expirée. */
    data object AuthExpiree : ResultatEnvoi

    /** 412 : la ressource a changé côté serveur. On conserve les données serveur pour la résolution. */
    data class Conflit(val versionServeur: Int?, val donneesServeurJson: String?) : ResultatEnvoi
}

/** Persistance de la file (implémentée par Room ; en mémoire dans les tests). */
interface FileOperations {
    /** Opérations à traiter, dans l'ordre d'enregistrement (FIFO). */
    suspend fun operationsAEnvoyer(): List<OperationEnAttente>

    suspend fun mettreAJour(operation: OperationEnAttente)

    suspend fun supprimer(id: Long)

    /** Après un arrêt brutal (process tué, redémarrage) : `EN_COURS` → `EN_ATTENTE`. */
    suspend fun recupererOrphelines()
}

/** Envoi réseau d'une opération (implémenté au-dessus d'OkHttp ; simulé dans les tests). */
interface PasserelleDistante {
    suspend fun envoyer(operation: OperationEnAttente): ResultatEnvoi
}

/** Correspondance `@local:<uuid>` → id serveur, persistante (survit à un redémarrage). */
interface CorrespondancesIds {
    suspend fun resoudre(referenceLocale: String): String?
    suspend fun enregistrer(referenceLocale: String, idServeur: String)
}
