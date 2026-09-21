package com.spipme.app.ui.imports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.FichierSelectionne
import com.spipme.app.domain.repository.ImportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val EXTENSIONS_SUPPORTEES = setOf("csv", "xlsx", "xls")

@HiltViewModel
class ImportsViewModel @Inject constructor(
    private val importRepository: ImportRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportsUiState())
    val uiState: StateFlow<ImportsUiState> = _uiState.asStateFlow()

    private var secteurActifId: Int? = null

    init {
        viewModelScope.launch {
            val secteurId = sessionManager.secteurActifIdFlow.filterNotNull().first()
            val secteurNom = sessionManager.secteurActifNomFlow.first().orEmpty()
            secteurActifId = secteurId
            _uiState.update { it.copy(secteurActifNom = secteurNom) }
            chargerHistorique()
        }
    }

    fun rafraichir() {
        viewModelScope.launch { chargerHistorique() }
    }

    fun fichierSelectionne(fichier: FichierSelectionne) {
        val extension = fichier.nom.substringAfterLast('.', "").lowercase()
        if (extension !in EXTENSIONS_SUPPORTEES) {
            _uiState.update {
                it.copy(messageErreur = "Format non supporté : utilisez un fichier .csv ou .xlsx.")
            }
            return
        }
        _uiState.update {
            it.copy(
                fichierSelectionne = fichier,
                etape = EtapeImport.SELECTION,
                apercu = null,
                resultatImport = null,
                messageErreur = null,
            )
        }
    }

    fun lancerApercu() {
        val secteurId = secteurActifId ?: return
        val fichier = _uiState.value.fichierSelectionne ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(etape = EtapeImport.APERCU_EN_COURS, messageErreur = null) }
            when (val resultat = importRepository.previsualiser(secteurId, fichier)) {
                is Resultat.Succes -> _uiState.update { it.copy(etape = EtapeImport.APERCU_PRET, apercu = resultat.donnees) }
                is Resultat.Echec -> _uiState.update {
                    it.copy(etape = EtapeImport.SELECTION, messageErreur = resultat.message)
                }
            }
        }
    }

    fun confirmerImport() {
        val secteurId = secteurActifId ?: return
        val fichier = _uiState.value.fichierSelectionne ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(etape = EtapeImport.IMPORT_EN_COURS, messageErreur = null) }
            when (val resultat = importRepository.importer(secteurId, fichier)) {
                is Resultat.Succes -> {
                    _uiState.update {
                        it.copy(etape = EtapeImport.TERMINE, resultatImport = resultat.donnees, fichierSelectionne = null, apercu = null)
                    }
                    chargerHistorique()
                }
                is Resultat.Echec -> _uiState.update {
                    it.copy(etape = EtapeImport.APERCU_PRET, messageErreur = resultat.message)
                }
            }
        }
    }

    fun recommencer() {
        _uiState.update {
            it.copy(etape = EtapeImport.SELECTION, fichierSelectionne = null, apercu = null, resultatImport = null, messageErreur = null)
        }
    }

    private suspend fun chargerHistorique() {
        val secteurId = secteurActifId ?: return
        _uiState.update { it.copy(enChargementHistorique = true) }
        when (val resultat = importRepository.obtenirHistorique(secteurId)) {
            is Resultat.Succes -> _uiState.update { it.copy(historique = resultat.donnees, enChargementHistorique = false) }
            is Resultat.Echec -> _uiState.update { it.copy(messageErreur = resultat.message, enChargementHistorique = false) }
        }
    }
}
