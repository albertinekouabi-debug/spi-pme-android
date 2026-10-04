package com.spipme.app.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Secteur
import com.spipme.app.domain.repository.SecteurRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecteurSelectorUiState(
    val ouvert: Boolean = false,
    val chargement: Boolean = false,
    val secteurs: List<Secteur> = emptyList(),
    val messageErreur: String? = null,
)

@HiltViewModel
class SecteurSelectorViewModel @Inject constructor(
    private val secteurRepository: SecteurRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecteurSelectorUiState())
    val uiState: StateFlow<SecteurSelectorUiState> = _uiState.asStateFlow()

    fun ouvrir() {
        _uiState.update { it.copy(ouvert = true, messageErreur = null) }
        if (_uiState.value.secteurs.isEmpty()) charger()
    }

    fun fermer() {
        _uiState.update { it.copy(ouvert = false) }
    }

    private fun charger() {
        viewModelScope.launch {
            _uiState.update { it.copy(chargement = true) }
            when (val resultat = secteurRepository.mesSecteurs()) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(chargement = false, secteurs = resultat.donnees)
                }
                is Resultat.Echec -> _uiState.update {
                    it.copy(chargement = false, messageErreur = resultat.message)
                }
            }
        }
    }

    /** Change le secteur actif localement. Chaque appel API suivant le transmet en paramètre ;
     * le serveur l'intersecte TOUJOURS avec les secteurs réellement autorisés — jamais de confiance aveugle au client. */
    fun selectionner(secteur: Secteur) {
        viewModelScope.launch {
            sessionManager.changerSecteurActif(secteur.id, secteur.nom)
            _uiState.update { it.copy(ouvert = false) }
        }
    }
}

