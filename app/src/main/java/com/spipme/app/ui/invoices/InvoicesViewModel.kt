package com.spipme.app.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Facture
import com.spipme.app.domain.repository.FactureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoicesUiState(
    val chargementInitial: Boolean = true,
    val chargementPageSuivante: Boolean = false,
    val secteurActifNom: String = "",
    val factures: List<Facture> = emptyList(),
    val filtreStatut: String? = null,
    val pageCourante: Int = 1,
    val ilResteDesPages: Boolean = false,
    val messageErreur: String? = null,
) {
    val estVide: Boolean get() = !chargementInitial && factures.isEmpty() && messageErreur == null
}

@HiltViewModel
class InvoicesViewModel @Inject constructor(
    private val factureRepository: FactureRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoicesUiState())
    val uiState: StateFlow<InvoicesUiState> = _uiState.asStateFlow()

    private var secteurActifId: Int? = null

    init {
        viewModelScope.launch {
            secteurActifId = sessionManager.secteurActifIdFlow.filterNotNull().first()
            val nom = sessionManager.secteurActifNomFlow.first().orEmpty()
            _uiState.update { it.copy(secteurActifNom = nom) }
            charger()
        }
    }

    fun rafraichir() {
        viewModelScope.launch { charger() }
    }

    fun surSelectionStatut(statut: String?) {
        _uiState.update { it.copy(filtreStatut = statut) }
        viewModelScope.launch { charger() }
    }

    private suspend fun charger() {
        val secteurId = secteurActifId ?: return
        _uiState.update { it.copy(chargementInitial = true, messageErreur = null, pageCourante = 1) }

        when (val resultat = factureRepository.lister(secteurId, _uiState.value.filtreStatut, page = 1)) {
            is Resultat.Succes -> _uiState.update {
                it.copy(
                    chargementInitial = false,
                    factures = resultat.donnees.first,
                    ilResteDesPages = resultat.donnees.second,
                )
            }
            is Resultat.Echec -> _uiState.update {
                it.copy(chargementInitial = false, messageErreur = resultat.message)
            }
        }
    }

    fun chargerPageSuivante() {
        val etat = _uiState.value
        val secteurId = secteurActifId ?: return
        if (etat.chargementPageSuivante || !etat.ilResteDesPages) return

        viewModelScope.launch {
            _uiState.update { it.copy(chargementPageSuivante = true) }
            val pageSuivante = etat.pageCourante + 1
            when (val resultat = factureRepository.lister(secteurId, etat.filtreStatut, pageSuivante)) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(
                        chargementPageSuivante = false,
                        factures = it.factures + resultat.donnees.first,
                        pageCourante = pageSuivante,
                        ilResteDesPages = resultat.donnees.second,
                    )
                }
                is Resultat.Echec -> _uiState.update {
                    it.copy(chargementPageSuivante = false, messageErreur = resultat.message)
                }
            }
        }
    }
}

