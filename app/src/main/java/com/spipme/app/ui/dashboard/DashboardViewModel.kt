package com.spipme.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Alerte
import com.spipme.app.domain.model.PointSolde
import com.spipme.app.domain.model.ResumeAlertes
import com.spipme.app.domain.model.ResumeRegistre
import com.spipme.app.domain.model.ResumeRessources
import com.spipme.app.domain.model.ResumeSuggestions
import com.spipme.app.domain.model.ResumeTaches
import com.spipme.app.domain.model.ResumeTresorerie
import com.spipme.app.domain.model.Suggestion
import com.spipme.app.domain.model.Transaction
import com.spipme.app.domain.repository.AlerteRepository
import com.spipme.app.domain.repository.EntiteRepository
import com.spipme.app.domain.repository.RessourceRepository
import com.spipme.app.domain.repository.SuggestionRepository
import com.spipme.app.domain.repository.TacheRepository
import com.spipme.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.math.BigDecimal
import javax.inject.Inject

data class ActiviteDashboard(
    val id: String,
    val categorie: String,
    val titre: String,
    val detail: String,
    val dateIso: String,
)

data class DashboardUiState(
    val chargement: Boolean = true,
    val secteurActifNom: String = "",
    val resumeTresorerie: ResumeTresorerie? = null,
    val resumeRegistre: ResumeRegistre? = null,
    val resumeRessources: ResumeRessources? = null,
    val resumeTaches: ResumeTaches? = null,
    val resumeAlertes: ResumeAlertes? = null,
    val resumeSuggestions: ResumeSuggestions? = null,
    val evolutionTresorerie: List<PointSolde> = emptyList(),
    val activitesRecentes: List<ActiviteDashboard> = emptyList(),
    val suggestionsRecentes: List<Suggestion> = emptyList(),
    val messageErreur: String? = null,
) {
    val echecComplet: Boolean get() = !chargement && listOf(
        resumeTresorerie,
        resumeRegistre,
        resumeRessources,
        resumeTaches,
        resumeAlertes,
        resumeSuggestions,
    ).all { it == null } && activitesRecentes.isEmpty() && suggestionsRecentes.isEmpty() && evolutionTresorerie.size < 2
}

/** Agrège les résumés et historiques réellement fournis par les repositories du projet. */
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
    private var chargementJob: Job? = null

    init { charger() }

    fun rafraichir() = charger()

    private fun charger() {
        if (chargementJob?.isActive == true) return

        chargementJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    chargement = true,
                    resumeTresorerie = null,
                    resumeRegistre = null,
                    resumeRessources = null,
                    resumeTaches = null,
                    resumeAlertes = null,
                    resumeSuggestions = null,
                    evolutionTresorerie = emptyList(),
                    activitesRecentes = emptyList(),
                    suggestionsRecentes = emptyList(),
                    messageErreur = null,
                )
            }

            val secteurNom = withTimeoutOrNull(2_000L) {
                try {
                    sessionManager.secteurActifNomFlow.first().orEmpty()
                } catch (annulation: CancellationException) {
                    throw annulation
                } catch (_: Exception) {
                    ""
                }
            }.orEmpty()

            val secteurId = withTimeoutOrNull(5_000L) {
                sessionManager.secteurActifIdFlow.filterNotNull().first()
            }

            if (secteurId == null) {
                _uiState.update {
                    it.copy(
                        chargement = false,
                        secteurActifNom = secteurNom,
                        messageErreur = "Aucun secteur actif n’est disponible. Choisissez un secteur puis réessayez.",
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(secteurActifNom = secteurNom) }

            coroutineScope {
                launch {
                    val resultat = resultatSecurise { transactionRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeTresorerie = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { entiteRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeRegistre = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { ressourceRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeRessources = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { tacheRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeTaches = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { alerteRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeAlertes = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { suggestionRepository.obtenirResume(secteurId) }
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(resumeSuggestions = resultat.donnees) }
                }
                launch {
                    val resultat = resultatSecurise { transactionRepository.obtenirEvolution(secteurId, jours = 7) }
                    if (resultat is Resultat.Succes) {
                        _uiState.update { it.copy(evolutionTresorerie = resultat.donnees.sortedBy(PointSolde::date)) }
                    }
                }
                launch {
                    val resultat = resultatSecurise { transactionRepository.lister(secteurId) }
                    if (resultat is Resultat.Succes) {
                        ajouterActivites(resultat.donnees.map(::activiteTransaction))
                    }
                }
                launch {
                    val resultat = resultatSecurise { alerteRepository.lister(secteurId, statut = null, niveau = null) }
                    if (resultat is Resultat.Succes) {
                        ajouterActivites(resultat.donnees.map(::activiteAlerte))
                    }
                }
                launch {
                    val resultat = resultatSecurise { suggestionRepository.lister(secteurId, statut = "en_attente") }
                    if (resultat is Resultat.Succes) {
                        val suggestions = resultat.donnees
                            .filter { it.estEnAttente }
                            .sortedByDescending { it.dateCreation }
                            .take(3)
                        _uiState.update { it.copy(suggestionsRecentes = suggestions) }
                        ajouterActivites(suggestions.map(::activiteSuggestion))
                    }
                }
            }

            val final = _uiState.value
            val resumesIndisponibles = listOf(
                final.resumeTresorerie,
                final.resumeRegistre,
                final.resumeRessources,
                final.resumeTaches,
                final.resumeAlertes,
                final.resumeSuggestions,
            ).all { it == null }
            val sourcesPartielles = listOf(
                final.resumeTresorerie,
                final.resumeRegistre,
                final.resumeRessources,
                final.resumeTaches,
                final.resumeAlertes,
                final.resumeSuggestions,
            ).any { it == null }

            _uiState.update {
                it.copy(
                    chargement = false,
                    messageErreur = when {
                        resumesIndisponibles && it.activitesRecentes.isEmpty() && it.suggestionsRecentes.isEmpty() && it.evolutionTresorerie.size < 2 ->
                            "Les données n’ont pas pu être chargées. Vérifiez la connexion puis réessayez."
                        sourcesPartielles ->
                            "Certaines données sont temporairement indisponibles. Les autres sections restent accessibles."
                        else -> null
                    },
                )
            }
        }
    }

    private fun ajouterActivites(nouvelles: List<ActiviteDashboard>) {
        if (nouvelles.isEmpty()) return
        _uiState.update { ancien ->
            ancien.copy(
                activitesRecentes = (ancien.activitesRecentes + nouvelles)
                    .distinctBy { it.id }
                    .sortedByDescending { it.dateIso }
                    .take(5),
            )
        }
    }

    private fun activiteTransaction(transaction: Transaction) = ActiviteDashboard(
        id = "transaction-${transaction.id}",
        categorie = "transaction",
        titre = transaction.reference.ifBlank { libelleTypeTransaction(transaction.type) },
        detail = listOfNotNull(
            transaction.description.takeIf { it.isNotBlank() },
            transaction.entiteNom?.takeIf { it.isNotBlank() },
            transaction.montant?.let { "${it.stripTrailingZeros().toPlainString()} ${transaction.devise}" },
        ).joinToString(" · ").ifBlank { libelleTypeTransaction(transaction.type) },
        dateIso = transaction.dateTransaction,
    )

    private fun activiteAlerte(alerte: Alerte) = ActiviteDashboard(
        id = "alerte-${alerte.id}",
        categorie = "alerte",
        titre = alerte.titre,
        detail = listOfNotNull(
            alerte.description.takeIf { it.isNotBlank() },
            alerte.ressourceNom,
            alerte.entiteNom,
        ).joinToString(" · "),
        dateIso = alerte.dateDeclenchement,
    )

    private fun activiteSuggestion(suggestion: Suggestion) = ActiviteDashboard(
        id = "suggestion-${suggestion.id}",
        categorie = "suggestion",
        titre = suggestion.titre,
        detail = suggestion.description.ifBlank { suggestion.categorie },
        dateIso = suggestion.dateCreation,
    )

    private fun libelleTypeTransaction(type: String): String = when (type.lowercase()) {
        "entree" -> "Entrée de fonds"
        "sortie" -> "Sortie de fonds"
        "mouvement_stock" -> "Mouvement de stock"
        else -> type.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

    private suspend fun <T> resultatSecurise(action: suspend () -> Resultat<T>): Resultat<T>? =
        withTimeoutOrNull(15_000L) {
            try {
                action()
            } catch (annulation: CancellationException) {
                throw annulation
            } catch (_: Exception) {
                null
            }
        }
}