package com.spipme.app.ui.treasury

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TreasuryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TreasuryUiState())
    val uiState: StateFlow<TreasuryUiState> = _uiState.asStateFlow()

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

    fun surChangementOnglet(onglet: OngletTresorerie) {
        _uiState.update { it.copy(ongletActif = onglet) }
        viewModelScope.launch { chargerTransactions() }
    }

    fun rafraichir() {
        viewModelScope.launch { chargerTout() }
    }

    private suspend fun chargerTout() {
        _uiState.update { it.copy(enChargement = true, messageErreur = null) }
        kotlinx.coroutines.coroutineScope {
            launch { chargerResume() }
            launch { chargerEvolution() }
            launch { chargerTransactions() }
        }
        _uiState.update { it.copy(enChargement = false) }
    }

    private suspend fun chargerResume() {
        val secteurId = secteurActifId ?: return
        when (val resultat = transactionRepository.obtenirResume(secteurId)) {
            is Resultat.Succes -> _uiState.update { it.copy(resume = resultat.donnees) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }

    private suspend fun chargerEvolution() {
        val secteurId = secteurActifId ?: return
        when (val resultat = transactionRepository.obtenirEvolution(secteurId)) {
            is Resultat.Succes -> _uiState.update { it.copy(evolution = resultat.donnees) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }

    private suspend fun chargerTransactions() {
        val secteurId = secteurActifId ?: return
        val type = _uiState.value.ongletActif.typeFiltre
        when (val resultat = transactionRepository.lister(secteurId, type)) {
            is Resultat.Succes -> _uiState.update { it.copy(transactions = resultat.donnees, messageErreur = null) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }
}
