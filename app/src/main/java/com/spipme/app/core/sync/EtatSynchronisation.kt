package com.spipme.app.core.sync

/** Ce que l'utilisateur doit comprendre d'un coup d'œil (du plus urgent au moins urgent). */
enum class ResumeSynchronisation { HORS_LIGNE, CONFLITS, ECHECS, EN_COURS, EN_ATTENTE, A_JOUR }

data class EtatSynchronisation(
    val enLigne: Boolean = true,
    val enAttente: Int = 0,
    val conflits: Int = 0,
    val echecs: Int = 0,
    val enCours: Boolean = false,
) {
    /**
     * Priorité volontaire : un conflit ou un échec exige une décision humaine et doit toujours
     * passer devant « hors ligne » ; en revanche hors ligne passe devant « en cours » (aucun envoi
     * n'est possible) et devant « en attente ».
     */
    val resume: ResumeSynchronisation
        get() = when {
            conflits > 0 -> ResumeSynchronisation.CONFLITS
            echecs > 0 -> ResumeSynchronisation.ECHECS
            !enLigne -> ResumeSynchronisation.HORS_LIGNE
            enCours -> ResumeSynchronisation.EN_COURS
            enAttente > 0 -> ResumeSynchronisation.EN_ATTENTE
            else -> ResumeSynchronisation.A_JOUR
        }

    /** Message affiché dans le bandeau ; null = rien à afficher (tout est synchronisé). */
    val message: String?
        get() = when (resume) {
            ResumeSynchronisation.CONFLITS -> "$conflits conflit${s(conflits)} à résoudre"
            ResumeSynchronisation.ECHECS -> "$echecs opération${s(echecs)} en échec"
            ResumeSynchronisation.HORS_LIGNE ->
                if (enAttente > 0) "Hors ligne — $enAttente modification${s(enAttente)} en attente" else "Hors ligne"
            ResumeSynchronisation.EN_COURS -> "Synchronisation en cours…"
            ResumeSynchronisation.EN_ATTENTE -> "$enAttente modification${s(enAttente)} en attente de synchronisation"
            ResumeSynchronisation.A_JOUR -> null
        }

    private fun s(n: Int) = if (n > 1) "s" else ""
}
