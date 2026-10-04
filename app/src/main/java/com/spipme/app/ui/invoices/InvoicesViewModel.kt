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
import kotlinx.coroutines.flow.combine
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
    val peutAnnuler: Boolean = false,
    val idsEnAttente: Set<Int> = emptySet(),
    val factureAAnnuler: Facture? = null,
    val messageInfo: String? = null,
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
        // Droits (indicatifs, le serveur reste l'autorité) : masquer l'action plutôt que provoquer un 403.
        viewModelScope.launch {
            combine(sessionManager.permissionsFlow, sessionManager.roleNomFlow) { permissions, role ->
                role == "Administrateur" || "treasury.write" in permissions
            }.collect { autorise -> _uiState.update { it.copy(peutAnnuler = autorise) } }
        }
        // Quand une écriture en attente se termine (synchronisée), la liste est rechargée : l'état affiché
        // vient du serveur, jamais d'une supposition locale.
        viewModelScope.launch {
            var precedent = emptySet<Int>()
            factureRepository.idsAvecOperationEnAttente().collect { ids ->
                _uiState.update { it.copy(idsEnAttente = ids) }
                if ((precedent - ids).isNotEmpty() && secteurActifId != null) charger()
                precedent = ids
            }
        }
    }

    /** Une facture annulée/avoir, ou déjà en cours d'annulation, ne peut pas être annulée (actions incompatibles). */
    fun peutEtreAnnulee(facture: Facture): Boolean {
        val etat = _uiState.value
        return etat.peutAnnuler && facture.statut !in STATUTS_NON_ANNULABLES && facture.id !in etat.idsEnAttente
    }

    fun demanderAnnulation(facture: Facture) {
        if (peutEtreAnnulee(facture)) _uiState.update { it.copy(factureAAnnuler = facture, messageErreur = null) }
    }

    fun abandonnerAnnulation() { _uiState.update { it.copy(factureAAnnuler = null) } }

    fun confirmerAnnulation(motif: String) {
        val facture = _uiState.value.factureAAnnuler ?: return
        if (motif.isBlank()) {
            _uiState.update { it.copy(messageErreur = "Le motif d'annulation est obligatoire.") }
            return
        }
        viewModelScope.launch {
            when (val resultat = factureRepository.annuler(facture.id, motif)) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(
                        factureAAnnuler = null, messageErreur = null,
                        messageInfo = "Annulation enregistrée : elle sera synchronisée dès que possible.",
                    )
                }
                is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
            }
        }
    }

    private companion object {
        val STATUTS_NON_ANNULABLES = setOf("annulee", "avoir")
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
        _uiState.update { it.copy(chargementInitial = true, messageErreur = null, messageInfo = null, pageCourante = 1) }

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

