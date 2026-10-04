package com.spipme.app.ui.treasury

import kotlinx.coroutines.flow.combine
import com.spipme.app.ui.sync.versElementEnAttente
import com.spipme.app.domain.model.Transaction
import com.spipme.app.core.sync.android.SynchronisationRepository
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
    private val synchronisation: SynchronisationRepository,
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

        viewModelScope.launch {
            combine(sessionManager.permissionsFlow, sessionManager.roleNomFlow) { permissions, role ->
                role == "Administrateur" || "treasury.write" in permissions
            }.collect { autorise -> _uiState.update { it.copy(peutCorriger = autorise) } }
        }
        viewModelScope.launch {
            var precedent = emptySet<Int>()
            transactionRepository.idsAvecOperationEnAttente().collect { ids ->
                _uiState.update { it.copy(idsEnAttente = ids) }
                if ((precedent - ids).isNotEmpty() && secteurActifId != null) rafraichir()
                precedent = ids
            }
        }
        viewModelScope.launch {
            var precedent = 0
            synchronisation.operationsEnAttente("CREER_TRANSACTION").collect { operations ->
                _uiState.update { it.copy(creationsEnAttente = operations.map { op -> op.versElementEnAttente() }) }
                if (operations.size < precedent && secteurActifId != null) rafraichir()
                precedent = operations.size
            }
        }
    }

    /** Seule une transaction VALIDÉE, qui n'est pas déjà corrigée ni une contre-écriture, se contre-passe. */
    fun peutEtreContrePassee(transaction: Transaction): Boolean {
        val etat = _uiState.value
        return etat.peutCorriger && transaction.contrePassable && transaction.id !in etat.idsEnAttente
    }

    fun demanderContrePassation(transaction: Transaction) {
        if (peutEtreContrePassee(transaction)) _uiState.update { it.copy(transactionAContrePasser = transaction, erreurContrePassation = null) }
    }

    fun abandonnerContrePassation() { _uiState.update { it.copy(transactionAContrePasser = null, erreurContrePassation = null) } }

    fun confirmerContrePassation(motif: String) {
        val transaction = _uiState.value.transactionAContrePasser ?: return
        if (motif.isBlank()) { _uiState.update { it.copy(erreurContrePassation = "Le motif est obligatoire.") }; return }
        viewModelScope.launch {
            when (val resultat = transactionRepository.contrePasser(transaction.id, motif)) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(transactionAContrePasser = null, erreurContrePassation = null,
                        messageInfo = "Contre-écriture enregistrée : elle sera synchronisée dès que possible.")
                }
                is Resultat.Echec -> _uiState.update { it.copy(erreurContrePassation = resultat.message) }
            }
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
