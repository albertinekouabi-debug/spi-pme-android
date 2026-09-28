package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.EvenementAudit
import com.spipme.app.domain.model.ResumeAudit

/**
 * Journal d'audit : LECTURE SEULE. Aucune mÃ©thode de modification ou de
 * suppression n'est dÃ©clarÃ©e volontairement â€” l'intÃ©gritÃ© du journal est
 * une exigence, pas une option (protections ORM + trigger cÃ´tÃ© serveur).
 */
interface AuditRepository {
    suspend fun lister(
        module: String? = null,
        resultat: String? = null,
        recherche: String? = null,
        page: Int? = null,
    ): Resultat<Pair<List<EvenementAudit>, Boolean>>

    suspend fun obtenirResume(
        module: String? = null,
        resultat: String? = null,
        recherche: String? = null,
    ): Resultat<ResumeAudit>
}

