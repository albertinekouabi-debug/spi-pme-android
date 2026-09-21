package com.spipme.app.ui.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.RessourceRepository
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
class ResourcesViewModel @Inject constructor(
    private val ressourceRepository: RessourceRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResourcesUiState())
    val uiState: StateFlow<ResourcesUiState> = _uiState.asStateFlow()

    private var secteurActifId: Int? = null

    init {
        viewModelScope.launch {
            val secteurId = sessionManager.secteurActifIdFlow.filterNotNull().first()
            val secteurNom = sessionManager.secteurActifNomFlow.first().orEmpty()
            secteurActifId = secteurId
            _uiState.update { it.copy(secteurActifNom = secteurNom) }
            chargerTout()
        }

        _uiState
            .map { it.recherche }
            .distinctUntilChanged()
            .drop(1)
            .debounce(350)
            .onEach { chargerRessources() }
            .launchIn(viewModelScope)
    }

    fun surChangementOnglet(onglet: OngletRessources) {
        _uiState.update { it.copy(ongletActif = onglet) }
        viewModelScope.launch { chargerRessources() }
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
            launch { chargerRessources() }
        }
        _uiState.update { it.copy(enChargement = false) }
    }

    private suspend fun chargerResume() {
        val secteurId = secteurActifId ?: return
        when (val resultat = ressourceRepository.obtenirResume(secteurId)) {
            is Resultat.Succes -> _uiState.update { it.copy(resume = resultat.donnees) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }

    private suspend fun chargerRessources() {
        val secteurId = secteurActifId ?: return
        val etat = _uiState.value
        when (
            val resultat = ressourceRepository.lister(
                secteurId = secteurId,
                statut = etat.ongletActif.statutFiltre,
                recherche = etat.recherche.ifBlank { null },
            )
        ) {
            is Resultat.Succes -> _uiState.update { it.copy(ressources = resultat.donnees, messageErreur = null) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message) }
        }
    }
}
