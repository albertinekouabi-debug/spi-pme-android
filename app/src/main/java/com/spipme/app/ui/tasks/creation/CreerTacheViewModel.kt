package com.spipme.app.ui.tasks.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.TacheRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val REGEX_DATE_ISO = Regex("""^\d{4}-\d{2}-\d{2}$""")

@HiltViewModel
class CreerTacheViewModel @Inject constructor(
    private val tacheRepository: TacheRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreerTacheUiState())
    val uiState: StateFlow<CreerTacheUiState> = _uiState.asStateFlow()

    fun surChangementTitre(v: String) = _uiState.update { it.copy(titre = v, messageErreur = null) }
    fun surChangementDescription(v: String) = _uiState.update { it.copy(description = v) }
    fun surChangementCategorie(v: String) = _uiState.update { it.copy(categorie = v) }
    fun surChangementPriorite(v: String) = _uiState.update { it.copy(priorite = v) }
    fun surChangementEcheance(v: String) = _uiState.update { it.copy(echeance = v, messageErreur = null) }

    fun enregistrer() {
        val etat = _uiState.value
        if (etat.titre.isBlank()) {
            _uiState.update { it.copy(messageErreur = "Le titre est obligatoire.") }
            return
        }
        if (etat.echeance.isNotBlank() && !REGEX_DATE_ISO.matches(etat.echeance.trim())) {
            _uiState.update { it.copy(messageErreur = "Échéance invalide — format attendu AAAA-MM-JJ.") }
            return
        }

        viewModelScope.launch {
            val secteurId = sessionManager.secteurActifIdFlow.first()
            if (secteurId == null) {
                _uiState.update { it.copy(messageErreur = "Aucun secteur actif — reconnectez-vous.") }
                return@launch
            }

            _uiState.update { it.copy(enCoursDEnvoi = true, messageErreur = null) }
            val resultat = tacheRepository.creer(
                secteurId = secteurId,
                titre = etat.titre,
                description = etat.description.ifBlank { null },
                categorie = etat.categorie.ifBlank { null },
                priorite = etat.priorite,
                echeanceIso = etat.echeance.trim().ifBlank { null },
            )
            when (resultat) {
                is Resultat.Succes -> _uiState.update { it.copy(enCoursDEnvoi = false, creationReussie = true) }
                is Resultat.Echec -> _uiState.update {
                    // Saisie conservée hors ligne : c'est un SUCCÈS pour l'utilisateur (le bandeau de synchronisation
                    // indique qu'elle attend le réseau). Seul un vrai refus (validation, droits...) est une erreur.
                    if (resultat.enFile) it.copy(enCoursDEnvoi = false, creationReussie = true)
                    else it.copy(enCoursDEnvoi = false, messageErreur = resultat.message)
                }
            }
        }
    }
}
