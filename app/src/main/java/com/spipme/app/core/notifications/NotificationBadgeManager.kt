package com.spipme.app.core.notifications

import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.AlerteRepository
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Source unique du badge de notifications (audit — remplaçait un `0` codé en
 * dur dans 12 écrans). Réutilise `/alerts/summary/`, déjà appelé par l'écran
 * Alertes pour ses propres cartes : pas de nouvel endpoint (évite la
 * duplication signalée dans l'audit précédent).
 *
 * "Non lu" = alertes au statut "active" (ni traitée, ni ignorée) dans le
 * secteur actif de l'utilisateur. Il n'existe pas de notion de lecture
 * individuelle par utilisateur côté serveur : une alerte est une donnée
 * partagée par toute l'équipe du secteur, pas une notification personnelle.
 *
 * Portée `ActivityRetainedScoped` (survit aux rotations, meurt avec la
 * dernière Activity) plutôt que `Singleton` : évite de garder un compteur
 * obsolète en mémoire process après une déconnexion complète.
 */
@ActivityRetainedScoped
class NotificationBadgeManager @Inject constructor(
    private val alerteRepository: AlerteRepository,
    sessionManager: SessionManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _compte = MutableStateFlow(0)
    val compte: StateFlow<Int> = _compte.asStateFlow()

    init {
        // Se réaligne automatiquement quand l'utilisateur change de secteur actif
        // (sélecteur en en-tête) — évite d'afficher le badge d'un autre secteur.
        sessionManager.secteurActifIdFlow
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { secteurId -> rafraichir(secteurId) }
            .launchIn(scope)
    }

    /** Peut aussi être appelé manuellement après résolution/ignorance d'une alerte pour un retour immédiat. */
    fun rafraichir(secteurId: Int) {
        scope.launch {
            when (val resultat = alerteRepository.obtenirResume(secteurId)) {
                is Resultat.Succes -> {
                    val r = resultat.donnees
                    _compte.value = r.critiques + r.elevees + r.moderees
                }
                is Resultat.Echec -> {
                    // Échec réseau : on conserve la dernière valeur connue plutôt que
                    // d'afficher 0, qui serait trompeur (silence ≠ absence d'alerte).
                }
            }
        }
    }
}
