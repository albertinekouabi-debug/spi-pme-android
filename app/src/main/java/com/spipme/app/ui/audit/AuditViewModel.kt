package com.spipme.app.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditViewModel @Inject constructor(
    private val auditRepository: AuditRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditUiState())
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    /** Debounce de la recherche, même convention que Registre/Ressources (350 ms). */
    private var jobRecherche: Job? = null

    init {
        viewModelScope.launch {
            val secteurNom = sessionManager.secteurActifNomFlow.first().orEmpty()
            _uiState.update { it.copy(secteurActifNom = secteurNom) }
        }
        charger()
    }

    fun rafraichir() = charger()

    fun surChangementRecherche(valeur: String) {
        _uiState.update { it.copy(recherche = valeur) }
        jobRecherche?.cancel()
        jobRecherche = viewModelScope.launch {
            delay(350)
            charger()
        }
    }

    fun surSelectionModule(module: String?) {
        _uiState.update { it.copy(filtreModule = module) }
        charger()
    }

    fun surSelectionResultat(resultat: String?) {
        _uiState.update { it.copy(filtreResultat = resultat) }
        charger()
    }

    /** Charge la première page + le résumé en parallèle (les deux filtres identiques). */
    private fun charger() {
        viewModelScope.launch {
            val etat = _uiState.value
            _uiState.update { it.copy(chargementInitial = true, messageErreur = null, pageCourante = 1) }

            coroutineScope {
                launch {
                    val resultat = auditRepository.lister(
                        module = etat.filtreModule,
                        resultat = etat.filtreResultat,
                        recherche = etat.recherche.ifBlank { null },
                        page = 1,
                    )
                    when (resultat) {
                        is Resultat.Succes -> _uiState.update {
                            it.copy(
                                chargementInitial = false,
                                evenements = resultat.donnees.first,
                                ilResteDesPages = resultat.donnees.second,
                            )
                        }
                        is Resultat.Echec -> _uiState.update {
                            it.copy(chargementInitial = false, messageErreur = resultat.message)
                        }
                    }
                }
                launch {
                    val resultat = auditRepository.obtenirResume(
                        module = etat.filtreModule,
                        resultat = etat.filtreResultat,
                        recherche = etat.recherche.ifBlank { null },
                    )
                    if (resultat is Resultat.Succes) {
                        _uiState.update { it.copy(resume = resultat.donnees) }
                    }
                    // Un échec du seul résumé ne doit pas masquer la liste : l'erreur
                    // principale reste portée par le chargement de la liste ci-dessus.
                }
            }
        }
    }

    fun chargerPageSuivante() {
        val etat = _uiState.value
        if (etat.chargementPageSuivante || !etat.ilResteDesPages) return

        viewModelScope.launch {
            _uiState.update { it.copy(chargementPageSuivante = true) }
            val pageSuivante = etat.pageCourante + 1
            val resultat = auditRepository.lister(
                module = etat.filtreModule,
                resultat = etat.filtreResultat,
                recherche = etat.recherche.ifBlank { null },
                page = pageSuivante,
            )
            when (resultat) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(
                        chargementPageSuivante = false,
                        evenements = it.evenements + resultat.donnees.first,
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

