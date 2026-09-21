package com.spipme.app.ui.registry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.EntiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class RegistryViewModel @Inject constructor(
    private val entiteRepository: EntiteRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistryUiState())
    val uiState: StateFlow<RegistryUiState> = _uiState.asStateFlow()

    private var secteurActifId: Int? = null

    init {
        viewModelScope.launch {
            val secteurId = sessionManager.secteurActifIdFlow.filterNotNull().first()
            val secteurNom = sessionManager.secteurActifNomFlow.first().orEmpty()
            secteurActifId = secteurId
            _uiState.update { it.copy(secteurActifNom = secteurNom) }
            chargerTout()
        }

        // Recherche "vivante" avec anti-rebond : évite une requête réseau à chaque frappe.
        _uiState
            .map { it.recherche }
            .distinctUntilChanged()
            .drop(1) // la valeur initiale ne doit pas déclencher de recherche
            .debounce(350)
            .onEach { chargerEntites() }
            .launchIn(viewModelScope)
    }

    fun surChangementOnglet(onglet: OngletRegistre) {
        _uiState.update { it.copy(ongletActif = onglet) }
        viewModelScope.launch { chargerEntites() }
    }

    fun surChangementRecherche(valeur: String) {
        _uiState.update { it.copy(recherche = valeur) }
    }

    fun rafraichir() {
        viewModelScope.launch { chargerTout() }
    }

    private suspend fun chargerTout() {
        _uiState.update { it.copy(enChargement = true, messageErreur = null) }
        kotlinx.coroutines.coroutineScope {
            launch { chargerResume() }
            launch { chargerEntites() }
        }
        _uiState.update { it.copy(enChargement = false) }
    }

    private suspend fun chargerResume() {
        val secteurId = secteurActifId ?: return
        when (val resultat = entiteRepository.obtenirResume(secteurId)) {
            is Resultat.Succes -> _uiState.update { it.copy(resume = resultat.donnees) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }

    private suspend fun chargerEntites() {
        val secteurId = secteurActifId ?: return
        val etat = _uiState.value
        when (
            val resultat = entiteRepository.lister(
                secteurId = secteurId,
                type = etat.ongletActif.typeFiltre,
                recherche = etat.recherche.ifBlank { null },
            )
        ) {
            is Resultat.Succes -> _uiState.update { it.copy(entites = resultat.donnees, messageErreur = null) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }
}
