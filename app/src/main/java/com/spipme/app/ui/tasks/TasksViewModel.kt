package com.spipme.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.TacheRepository
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
class TasksViewModel @Inject constructor(
    private val tacheRepository: TacheRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

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

    fun surChangementOnglet(onglet: OngletTaches) {
        _uiState.update { it.copy(ongletActif = onglet) }
        viewModelScope.launch { chargerTaches() }
    }

    fun rafraichir() {
        viewModelScope.launch { chargerTout() }
    }

    fun marquerTerminee(tacheId: Int) {
        viewModelScope.launch {
            when (val resultat = tacheRepository.changerStatut(tacheId, "terminee")) {
                is Resultat.Succes -> chargerTout()
                is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
            }
        }
    }

    private suspend fun chargerTout() {
        _uiState.update { it.copy(enChargement = true, messageErreur = null) }
        coroutineScope {
            launch { chargerResume() }
            launch { chargerTaches() }
        }
        _uiState.update { it.copy(enChargement = false) }
    }

    private suspend fun chargerResume() {
        val secteurId = secteurActifId ?: return
        when (val resultat = tacheRepository.obtenirResume(secteurId)) {
            is Resultat.Succes -> _uiState.update { it.copy(resume = resultat.donnees) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }

    private suspend fun chargerTaches() {
        val secteurId = secteurActifId ?: return
        val onglet = _uiState.value.ongletActif
        val statut = when (onglet) {
            OngletTaches.EN_COURS -> "en_cours"
            OngletTaches.TERMINEES -> "terminee"
            else -> null
        }
        val retard = if (onglet == OngletTaches.EN_RETARD) true else null

        when (val resultat = tacheRepository.lister(secteurId, statut = statut, retard = retard)) {
            is Resultat.Succes -> _uiState.update { it.copy(taches = resultat.donnees, messageErreur = null) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }
}
