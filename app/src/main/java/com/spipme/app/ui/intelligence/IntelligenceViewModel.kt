package com.spipme.app.ui.intelligence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.SuggestionRepository
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

@HiltViewModel
class IntelligenceViewModel @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntelligenceUiState())
    val uiState: StateFlow<IntelligenceUiState> = _uiState.asStateFlow()

    private var secteurActifId: Int? = null

    init {
        viewModelScope.launch {
            val secteurId = sessionManager.secteurActifIdFlow.filterNotNull().first()
            val secteurNom = sessionManager.secteurActifNomFlow.first().orEmpty()
            secteurActifId = secteurId
            _uiState.update { it.copy(secteurActifNom = secteurNom) }
            chargerTout()
        }
    }

    fun surChangementOnglet(onglet: OngletSuggestions) {
        _uiState.update { it.copy(ongletActif = onglet) }
        viewModelScope.launch { chargerSuggestions() }
    }

    fun rafraichir() {
        viewModelScope.launch { chargerTout() }
    }

    fun genererSuggestions() {
        val secteurId = secteurActifId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(enCoursDeGeneration = true, messageErreur = null) }
            when (val resultat = suggestionRepository.genererPourSecteur(secteurId)) {
                is Resultat.Succes -> {
                    _uiState.update { it.copy(enCoursDeGeneration = false) }
                    chargerTout()
                }
                is Resultat.Echec -> _uiState.update { it.copy(enCoursDeGeneration = false, messageErreur = resultat.message) }
            }
        }
    }

    fun valider(suggestionId: Int) {
        viewModelScope.launch {
            when (val resultat = suggestionRepository.valider(suggestionId)) {
                is Resultat.Succes -> chargerTout()
                is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
            }
        }
    }

    fun ouvrirDialogueRejet(suggestionId: Int) {
        _uiState.update { it.copy(suggestionARejeterId = suggestionId) }
    }

    fun fermerDialogueRejet() {
        _uiState.update { it.copy(suggestionARejeterId = null) }
    }

    fun confirmerRejet(motif: String) {
        val suggestionId = _uiState.value.suggestionARejeterId ?: return
        if (motif.isBlank()) {
            _uiState.update { it.copy(messageErreur = "Le motif de rejet est obligatoire.") }
            return
        }
        viewModelScope.launch {
            when (val resultat = suggestionRepository.rejeter(suggestionId, motif)) {
                is Resultat.Succes -> {
                    _uiState.update { it.copy(suggestionARejeterId = null) }
                    chargerTout()
                }
                is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
            }
        }
    }

    private suspend fun chargerTout() {
        _uiState.update { it.copy(enChargement = true, messageErreur = null) }
        coroutineScope {
            launch { chargerResume() }
            launch { chargerSuggestions() }
        }
        _uiState.update { it.copy(enChargement = false) }
    }

    private suspend fun chargerResume() {
        val secteurId = secteurActifId ?: return
        when (val resultat = suggestionRepository.obtenirResume(secteurId)) {
            is Resultat.Succes -> _uiState.update { it.copy(resume = resultat.donnees) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }

    private suspend fun chargerSuggestions() {
        val secteurId = secteurActifId ?: return
        val statut = _uiState.value.ongletActif.statutFiltre
        when (val resultat = suggestionRepository.lister(secteurId, statut = statut)) {
            is Resultat.Succes -> _uiState.update { it.copy(suggestions = resultat.donnees, messageErreur = null) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }
}
