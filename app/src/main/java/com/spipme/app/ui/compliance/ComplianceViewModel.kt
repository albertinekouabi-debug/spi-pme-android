package com.spipme.app.ui.compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.DeclarationConformite
import com.spipme.app.domain.repository.ConformiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComplianceUiState(
    val chargementInitial: Boolean = true,
    val secteurActifNom: String = "",
    val declarations: List<DeclarationConformite> = emptyList(),
    val filtreStatut: String? = null,
    val ilResteDesPages: Boolean = false,
    val pageCourante: Int = 1,
    val chargementPageSuivante: Boolean = false,
    val actionEnCoursSurId: Int? = null,
    val messageErreur: String? = null,
    val messageSucces: String? = null,
) {
    val estVide: Boolean get() = !chargementInitial && declarations.isEmpty() && messageErreur == null
}

@HiltViewModel
class ComplianceViewModel @Inject constructor(
    private val conformiteRepository: ConformiteRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComplianceUiState())
    val uiState: StateFlow<ComplianceUiState> = _uiState.asStateFlow()

    private var secteurActifId: Int? = null

    init {
        viewModelScope.launch {
            secteurActifId = sessionManager.secteurActifIdFlow.filterNotNull().first()
            _uiState.update { it.copy(secteurActifNom = sessionManager.secteurActifNomFlow.first().orEmpty()) }
            charger()
        }
    }

    fun rafraichir() { viewModelScope.launch { charger() } }

    fun surSelectionStatut(statut: String?) {
        _uiState.update { it.copy(filtreStatut = statut) }
        viewModelScope.launch { charger() }
    }

    fun messageConsomme() {
        _uiState.update { it.copy(messageSucces = null, messageErreur = null) }
    }

    private suspend fun charger() {
        val secteurId = secteurActifId ?: return
        _uiState.update { it.copy(chargementInitial = true, messageErreur = null, pageCourante = 1) }
        when (val resultat = conformiteRepository.lister(secteurId, _uiState.value.filtreStatut, page = 1)) {
            is Resultat.Succes -> _uiState.update {
                it.copy(
                    chargementInitial = false,
                    declarations = resultat.donnees.first,
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
            val page = etat.pageCourante + 1
            when (val resultat = conformiteRepository.lister(secteurId, etat.filtreStatut, page)) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(
                        chargementPageSuivante = false,
                        declarations = it.declarations + resultat.donnees.first,
                        pageCourante = page,
                        ilResteDesPages = resultat.donnees.second,
                    )
                }
                is Resultat.Echec -> _uiState.update {
                    it.copy(chargementPageSuivante = false, messageErreur = resultat.message)
                }
            }
        }
    }

    /** POST /compliance-declarations/{id}/declare — la référence est obligatoire côté serveur. */
    fun declarer(id: Int, referenceDeclaration: String) {
        if (referenceDeclaration.isBlank()) {
            _uiState.update { it.copy(messageErreur = "La référence de déclaration est obligatoire.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionEnCoursSurId = id, messageErreur = null) }
            traiterResultatAction(conformiteRepository.declarer(id, referenceDeclaration.trim()), "Déclaration enregistrée.")
        }
    }

    /** POST /compliance-declarations/{id}/exempt — la note justificative est obligatoire côté serveur. */
    fun exempter(id: Int, note: String) {
        if (note.isBlank()) {
            _uiState.update { it.copy(messageErreur = "La note justificative est obligatoire.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionEnCoursSurId = id, messageErreur = null) }
            traiterResultatAction(conformiteRepository.exempter(id, note.trim()), "Exemption enregistrée.")
        }
    }

    /** Remplace l'élément en place plutôt que de recharger toute la liste. */
    private fun traiterResultatAction(resultat: Resultat<DeclarationConformite>, messageSucces: String) {
        when (resultat) {
            is Resultat.Succes -> _uiState.update { etat ->
                etat.copy(
                    actionEnCoursSurId = null,
                    messageSucces = messageSucces,
                    declarations = etat.declarations.map { if (it.id == resultat.donnees.id) resultat.donnees else it },
                )
            }
            is Resultat.Echec -> _uiState.update {
                it.copy(actionEnCoursSurId = null, messageErreur = resultat.message)
            }
        }
    }
}

