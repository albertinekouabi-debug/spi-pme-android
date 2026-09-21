package com.spipme.app.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.AlerteRepository
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
class AlertsViewModel @Inject constructor(
    private val alerteRepository: AlerteRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

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

    fun surChangementOnglet(onglet: OngletAlertes) {
        _uiState.update { it.copy(ongletActif = onglet) }
        viewModelScope.launch { chargerAlertes() }
    }

    fun rafraichir() {
        viewModelScope.launch { chargerTout() }
    }

    fun genererAlertes() {
        val secteurId = secteurActifId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(enCoursDeGeneration = true, messageErreur = null, messageInfo = null) }
            when (val resultat = alerteRepository.genererPourSecteur(secteurId)) {
                is Resultat.Succes -> {
                    val (creees, resolues) = resultat.donnees
                    _uiState.update {
                        it.copy(
                            enCoursDeGeneration = false,
                            messageInfo = "${creees.size} nouvelle(s) alerte(s), ${resolues.size} résolue(s) automatiquement.",
                        )
                    }
                    chargerTout()
                }
                is Resultat.Echec -> _uiState.update { it.copy(enCoursDeGeneration = false, messageErreur = resultat.message) }
            }
        }
    }

    fun resoudre(alerteId: Int) {
        viewModelScope.launch {
            when (val resultat = alerteRepository.resoudre(alerteId)) {
                is Resultat.Succes -> chargerTout()
                is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
            }
        }
    }

    fun ignorer(alerteId: Int) {
        viewModelScope.launch {
            when (val resultat = alerteRepository.ignorer(alerteId)) {
                is Resultat.Succes -> chargerTout()
                is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
            }
        }
    }

    private suspend fun chargerTout() {
        _uiState.update { it.copy(enChargement = true, messageErreur = null) }
        coroutineScope {
            launch { chargerResume() }
            launch { chargerAlertes() }
        }
        _uiState.update { it.copy(enChargement = false) }
    }

    private suspend fun chargerResume() {
        val secteurId = secteurActifId ?: return
        when (val resultat = alerteRepository.obtenirResume(secteurId)) {
            is Resultat.Succes -> _uiState.update { it.copy(resume = resultat.donnees) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }

    private suspend fun chargerAlertes() {
        val secteurId = secteurActifId ?: return
        val onglet = _uiState.value.ongletActif
        when (
            val resultat = alerteRepository.lister(secteurId, statut = onglet.statutFiltre, niveau = onglet.niveauFiltre)
        ) {
            is Resultat.Succes -> _uiState.update { it.copy(alertes = resultat.donnees, messageErreur = null) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }
}
