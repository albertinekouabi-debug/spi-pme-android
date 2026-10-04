package com.spipme.app.ui.pilotage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Pilotage
import com.spipme.app.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageCopilote(val question: String, val reponse: String? = null, val enCours: Boolean = true, val erreur: Boolean = false)

data class PilotageUiState(
    val enChargement: Boolean = true,
    val pilotage: Pilotage? = null,
    val messageErreur: String? = null,
    val secteurActifNom: String = "",
    val question: String = "",
    val messages: List<MessageCopilote> = emptyList(),
    val suggestions: List<String> = SUGGESTIONS_PAR_DEFAUT,
)

/** Questions que le copilote sait traiter (rappelées par le serveur à chaque réponse ; ce défaut sert hors ligne). */
val SUGGESTIONS_PAR_DEFAUT = listOf(
    "Quelle est la situation actuelle de mon entreprise ?",
    "Qu'est-ce qui nécessite mon attention ?",
    "Quels sont mes principaux risques ?",
    "Quelles anomalies ont été détectées ?",
    "Que prévoient les tendances actuelles ?",
    "Quels clients deviennent inactifs ?",
)

@HiltViewModel
class PilotageViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PilotageUiState())
    val uiState: StateFlow<PilotageUiState> = _uiState.asStateFlow()

    private var secteurActifId: Int? = null

    init {
        viewModelScope.launch {
            secteurActifId = sessionManager.secteurActifIdFlow.filterNotNull().first()
            _uiState.update { it.copy(secteurActifNom = sessionManager.secteurActifNomFlow.first().orEmpty()) }
            charger()
        }
    }

    fun rafraichir() { viewModelScope.launch { charger() } }

    private suspend fun charger() {
        val secteurId = secteurActifId ?: return
        _uiState.update { it.copy(enChargement = true, messageErreur = null) }
        when (val resultat = analyticsRepository.pilotage(secteurId)) {
            is Resultat.Succes -> _uiState.update { it.copy(enChargement = false, pilotage = resultat.donnees) }
            is Resultat.Echec -> _uiState.update { it.copy(enChargement = false, messageErreur = resultat.message) }
        }
    }

    fun surChangementQuestion(valeur: String) { _uiState.update { it.copy(question = valeur.take(500)) } }

    fun poser(texte: String = _uiState.value.question) {
        val question = texte.trim()
        val secteurId = secteurActifId
        if (question.isEmpty() || secteurId == null) return
        val index = _uiState.value.messages.size
        _uiState.update { it.copy(question = "", messages = it.messages + MessageCopilote(question)) }
        viewModelScope.launch {
            val resultat = analyticsRepository.interroger(question, secteurId)
            _uiState.update { etat ->
                val mis = etat.messages.toMutableList()
                mis[index] = when (resultat) {
                    is Resultat.Succes -> MessageCopilote(question, resultat.donnees.texte, enCours = false)
                    // Le copilote est une fonction DISTANTE : hors ligne on le dit, on ne simule jamais une réponse.
                    is Resultat.Echec -> MessageCopilote(
                        question,
                        if (resultat.reseau) "Le copilote nécessite une connexion au serveur : réessayez une fois en ligne." else resultat.message,
                        enCours = false, erreur = true,
                    )
                }
                val suggestions = (resultat as? Resultat.Succes)?.donnees?.questionsSupportees?.takeIf { it.isNotEmpty() } ?: etat.suggestions
                etat.copy(messages = mis, suggestions = suggestions)
            }
        }
    }
}
