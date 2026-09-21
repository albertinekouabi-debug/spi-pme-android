package com.spipme.app.ui.registry.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.EntiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreerEntiteViewModel @Inject constructor(
    private val entiteRepository: EntiteRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreerEntiteUiState())
    val uiState: StateFlow<CreerEntiteUiState> = _uiState.asStateFlow()

    fun surChangementType(valeur: String) = _uiState.update { it.copy(type = valeur, messageErreur = null) }
    fun surChangementTypePersonnalise(valeur: String) = _uiState.update { it.copy(typePersonnalise = valeur, messageErreur = null) }
    fun surChangementNom(valeur: String) = _uiState.update { it.copy(nom = valeur, messageErreur = null) }
    fun surChangementTelephone(valeur: String) = _uiState.update { it.copy(telephone = valeur) }
    fun surChangementEmail(valeur: String) = _uiState.update { it.copy(email = valeur) }
    fun surChangementVille(valeur: String) = _uiState.update { it.copy(ville = valeur) }

    fun enregistrer() {
        val etat = _uiState.value

        if (etat.nom.isBlank()) {
            _uiState.update { it.copy(messageErreur = "Le nom est obligatoire.") }
            return
        }
        if (etat.typeEffectif.isBlank()) {
            _uiState.update { it.copy(messageErreur = "Précisez le type d'entité.") }
            return
        }

        viewModelScope.launch {
            val secteurId = sessionManager.secteurActifIdFlow.first()
            if (secteurId == null) {
                _uiState.update { it.copy(messageErreur = "Aucun secteur actif — reconnectez-vous.") }
                return@launch
            }

            _uiState.update { it.copy(enCoursDEnvoi = true, messageErreur = null) }
            val resultat = entiteRepository.creer(
                secteurId = secteurId,
                type = etat.typeEffectif,
                nom = etat.nom,
                telephone = etat.telephone.ifBlank { null },
                email = etat.email.ifBlank { null },
                ville = etat.ville.ifBlank { null },
            )
            when (resultat) {
                is Resultat.Succes -> _uiState.update { it.copy(enCoursDEnvoi = false, creationReussie = true) }
                is Resultat.Echec -> _uiState.update { it.copy(enCoursDEnvoi = false, messageErreur = resultat.message) }
            }
        }
    }
}
