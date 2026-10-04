package com.spipme.app.ui.resources

import kotlinx.coroutines.flow.combine
import com.spipme.app.ui.sync.versElementEnAttente
import com.spipme.app.domain.model.Ressource
import com.spipme.app.core.sync.android.SynchronisationRepository
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
    private val synchronisation: SynchronisationRepository,
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

        // Droits indicatifs (le serveur reste l'autorité) : on masque l'action plutôt que de provoquer un 403.
        viewModelScope.launch {
            combine(sessionManager.permissionsFlow, sessionManager.roleNomFlow) { permissions, role ->
                role == "Administrateur" || "resources.write" in permissions
            }.collect { autorise -> _uiState.update { it.copy(peutModifier = autorise) } }
        }
        // Créations saisies hors ligne : visibles tout de suite dans la liste, marquées « en attente ».
        viewModelScope.launch {
            synchronisation.operationsEnAttente("CREER_RESSOURCE").collect { operations ->
                _uiState.update { it.copy(creationsEnAttente = operations.map { op -> op.versElementEnAttente() }) }
            }
        }
        // Quand une écriture en attente aboutit, la liste est rechargée depuis le serveur.
        viewModelScope.launch {
            var precedent = emptySet<Int>()
            synchronisation.entitesEnAttente("ressource").map { ids -> ids.mapNotNull { it.toIntOrNull() }.toSet() }.collect { ids ->
                _uiState.update { it.copy(idsModificationEnAttente = ids) }
                if ((precedent - ids).isNotEmpty() && secteurActifId != null) rafraichir()
                precedent = ids
            }
        }
        viewModelScope.launch {
            var precedent = 0
            synchronisation.operationsEnAttente("CREER_RESSOURCE").map { it.size }.collect { n ->
                if (n < precedent && secteurActifId != null) rafraichir()
                precedent = n
            }
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

    fun demanderModification(ressource: Ressource) {
        val etat = _uiState.value
        if (etat.peutModifier && ressource.id !in etat.idsModificationEnAttente) {
            _uiState.update { it.copy(ressourceAModifier = ressource, erreurModification = null) }
        }
    }

    fun abandonnerModification() { _uiState.update { it.copy(ressourceAModifier = null, erreurModification = null) } }

    /** Validation locale avant tout envoi/enfilage : une saisie invalide ne doit jamais entrer dans la file. */
    fun confirmerModification(nom: String, emplacement: String, seuilCritiqueTexte: String, seuilAlerteTexte: String) {
        val ressource = _uiState.value.ressourceAModifier ?: return
        if (nom.isBlank()) { erreur("Le nom est obligatoire."); return }
        val critique = seuilCritiqueTexte.trim().takeIf { it.isNotEmpty() }?.let {
            it.replace(',', '.').toBigDecimalOrNull() ?: run { erreur("Seuil critique invalide."); return }
        }
        val alerte = seuilAlerteTexte.trim().takeIf { it.isNotEmpty() }?.let {
            it.replace(',', '.').toBigDecimalOrNull() ?: run { erreur("Seuil d'alerte invalide."); return }
        }
        if ((critique != null && critique.signum() < 0) || (alerte != null && alerte.signum() < 0)) { erreur("Les seuils ne peuvent pas être négatifs."); return }
        if (critique != null && alerte != null && critique > alerte) { erreur("Le seuil critique doit être inférieur ou égal au seuil d'alerte."); return }

        viewModelScope.launch {
            when (val resultat = ressourceRepository.modifier(ressource, nom.trim(), emplacement.trim().ifEmpty { null }, critique, alerte)) {
                is Resultat.Succes -> {
                    _uiState.update { it.copy(ressourceAModifier = null, erreurModification = null, messageInfo = "Ressource modifiée.") }
                    rafraichir()
                }
                is Resultat.Echec -> _uiState.update {
                    if (resultat.enFile) it.copy(
                        ressourceAModifier = null, erreurModification = null,
                        messageInfo = "Modification enregistrée hors ligne : elle sera synchronisée dès le retour du réseau.",
                    ) else it.copy(erreurModification = resultat.message)
                }
            }
        }
    }

    private fun erreur(message: String) { _uiState.update { it.copy(erreurModification = message) } }

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
