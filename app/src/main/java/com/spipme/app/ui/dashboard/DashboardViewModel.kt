package com.spipme.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.ResumeAlertes
import com.spipme.app.domain.model.ResumeRegistre
import com.spipme.app.domain.model.ResumeRessources
import com.spipme.app.domain.model.ResumeSuggestions
import com.spipme.app.domain.model.ResumeTaches
import com.spipme.app.domain.model.ResumeTresorerie
import com.spipme.app.domain.repository.AlerteRepository
import com.spipme.app.domain.repository.EntiteRepository
import com.spipme.app.domain.repository.RessourceRepository
import com.spipme.app.domain.repository.SuggestionRepository
import com.spipme.app.domain.repository.TacheRepository
import com.spipme.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val chargement: Boolean = true,
    val secteurActifNom: String = "",
    val resumeTresorerie: ResumeTresorerie? = null,
    val resumeRegistre: ResumeRegistre? = null,
    val resumeRessources: ResumeRessources? = null,
    val resumeTaches: ResumeTaches? = null,
    val resumeAlertes: ResumeAlertes? = null,
    val resumeSuggestions: ResumeSuggestions? = null,
    val messageErreur: String? = null,
) {
    /** Vrai uniquement si TOUS les summaries ont Ã©chouÃ© â€” un Ã©chec partiel n'empÃªche pas d'afficher le reste. */
    val echecComplet: Boolean get() =
        !chargement && listOf(resumeTresorerie, resumeRegistre, resumeRessources, resumeTaches, resumeAlertes, resumeSuggestions)
            .all { it == null }
}

/**
 * Dashboard = agrÃ©gation CLIENT des `summary` dÃ©jÃ  existants et testÃ©s
 * (registry/resources/treasury/tasks/alerts/intelligence). DÃ©cision prise
 * lors du gap analysis initial : pas de nouvel endpoint /dashboard cÃ´tÃ©
 * serveur, pour Ã©viter un point de duplication de logique mÃ©tier â€” chaque
 * module reste seul responsable de son propre rÃ©sumÃ©.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val entiteRepository: EntiteRepository,
    private val ressourceRepository: RessourceRepository,
    private val tacheRepository: TacheRepository,
    private val alerteRepository: AlerteRepository,
    private val suggestionRepository: SuggestionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { charger() }

    fun rafraichir() = charger()

    private fun charger() {
        viewModelScope.launch {
            val secteurId = sessionManager.secteurActifIdFlow.filterNotNull().first()
            val secteurNom = sessionManager.secteurActifNomFlow.first().orEmpty()
            _uiState.update { it.copy(chargement = true, secteurActifNom = secteurNom, messageErreur = null) }

            // Chaque summary est chargÃ© indÃ©pendamment : l'Ã©chec d'un module
            // (ex. droits insuffisants sur Alertes) n'empÃªche jamais
            // l'affichage des autres cartes.
            coroutineScope {
                launch {
                    val r = transactionRepository.obtenirResume(secteurId)
                    if (r is Resultat.Succes) _uiState.update { it.copy(resumeTresorerie = r.donnees) }
                }
                launch {
                    val r = entiteRepository.obtenirResume(secteurId)
                    if (r is Resultat.Succes) _uiState.update { it.copy(resumeRegistre = r.donnees) }
                }
                launch {
                    val r = ressourceRepository.obtenirResume(secteurId)
                    if (r is Resultat.Succes) _uiState.update { it.copy(resumeRessources = r.donnees) }
                }
                launch {
                    val r = tacheRepository.obtenirResume(secteurId)
                    if (r is Resultat.Succes) _uiState.update { it.copy(resumeTaches = r.donnees) }
                }
                launch {
                    val r = alerteRepository.obtenirResume(secteurId)
                    if (r is Resultat.Succes) _uiState.update { it.copy(resumeAlertes = r.donnees) }
                }
                launch {
                    val r = suggestionRepository.obtenirResume(secteurId)
                    if (r is Resultat.Succes) _uiState.update { it.copy(resumeSuggestions = r.donnees) }
                }
            }
            _uiState.update { it.copy(chargement = false) }
        }
    }
}

