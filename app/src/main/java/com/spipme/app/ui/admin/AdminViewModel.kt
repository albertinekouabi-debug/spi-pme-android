package com.spipme.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Role
import com.spipme.app.domain.model.Secteur
import com.spipme.app.domain.model.Utilisateur
import com.spipme.app.domain.repository.AdminRepository
import com.spipme.app.domain.repository.SecteurRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val chargementInitial: Boolean = true,
    val chargementPageSuivante: Boolean = false,
    val utilisateurs: List<Utilisateur> = emptyList(),
    val roles: List<Role> = emptyList(),
    val secteurs: List<Secteur> = emptyList(),
    val pageCourante: Int = 1,
    val ilResteDesPages: Boolean = false,
    val afficherFormulaireCreation: Boolean = false,
    val creationEnCours: Boolean = false,
    val actionEnCoursSurId: Int? = null,
    val messageErreur: String? = null,
    val messageSucces: String? = null,
) {
    val estVide: Boolean get() = !chargementInitial && utilisateurs.isEmpty() && messageErreur == null
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val secteurRepository: SecteurRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init { charger() }

    fun rafraichir() = charger()

    private fun charger() {
        viewModelScope.launch {
            _uiState.update { it.copy(chargementInitial = true, messageErreur = null, pageCourante = 1) }
            coroutineScope {
                launch {
                    when (val resultat = adminRepository.listerUtilisateurs(page = 1)) {
                        is Resultat.Succes -> _uiState.update {
                            it.copy(
                                chargementInitial = false,
                                utilisateurs = resultat.donnees.first,
                                ilResteDesPages = resultat.donnees.second,
                            )
                        }
                        is Resultat.Echec -> _uiState.update {
                            it.copy(chargementInitial = false, messageErreur = resultat.message)
                        }
                    }
                }
                launch {
                    val resultat = adminRepository.listerRoles()
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(roles = resultat.donnees) }
                }
                launch {
                    val resultat = secteurRepository.mesSecteurs()
                    if (resultat is Resultat.Succes) _uiState.update { it.copy(secteurs = resultat.donnees) }
                }
            }
        }
    }

    fun chargerPageSuivante() {
        val etat = _uiState.value
        if (etat.chargementPageSuivante || !etat.ilResteDesPages) return
        viewModelScope.launch {
            _uiState.update { it.copy(chargementPageSuivante = true) }
            val page = etat.pageCourante + 1
            when (val resultat = adminRepository.listerUtilisateurs(page)) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(
                        chargementPageSuivante = false,
                        utilisateurs = it.utilisateurs + resultat.donnees.first,
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

    fun ouvrirFormulaireCreation() = _uiState.update { it.copy(afficherFormulaireCreation = true) }
    fun fermerFormulaireCreation() = _uiState.update { it.copy(afficherFormulaireCreation = false) }
    fun messageConsomme() = _uiState.update { it.copy(messageErreur = null, messageSucces = null) }

    fun creerUtilisateur(
        nomUtilisateur: String,
        email: String,
        motDePasse: String,
        roleId: Int,
        secteurPrincipalId: Int,
        nomComplet: String,
        telephone: String,
    ) {
        if (nomUtilisateur.isBlank() || email.isBlank() || motDePasse.length < 8) {
            _uiState.update { it.copy(messageErreur = "Nom d'utilisateur, email et mot de passe (8 caractÃ¨res min.) sont requis.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(creationEnCours = true, messageErreur = null) }
            val resultat = adminRepository.creerUtilisateur(
                nomUtilisateur = nomUtilisateur.trim(),
                email = email.trim(),
                motDePasse = motDePasse,
                roleId = roleId,
                secteurPrincipalId = secteurPrincipalId,
                nomComplet = nomComplet.trim().ifBlank { null },
                telephone = telephone.trim().ifBlank { null },
            )
            when (resultat) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(
                        creationEnCours = false,
                        afficherFormulaireCreation = false,
                        utilisateurs = listOf(resultat.donnees) + it.utilisateurs,
                        messageSucces = "Compte crÃ©Ã©.",
                    )
                }
                is Resultat.Echec -> _uiState.update {
                    it.copy(creationEnCours = false, messageErreur = resultat.message)
                }
            }
        }
    }

    /** DÃ©sactivation (soft delete cÃ´tÃ© serveur) â€” jamais de suppression physique. */
    fun desactiverUtilisateur(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionEnCoursSurId = id, messageErreur = null) }
            when (val resultat = adminRepository.desactiverUtilisateur(id)) {
                is Resultat.Succes -> _uiState.update { etat ->
                    etat.copy(
                        actionEnCoursSurId = null,
                        messageSucces = "Compte dÃ©sactivÃ©.",
                        utilisateurs = etat.utilisateurs.map { if (it.id == id) it.copy(actif = false) else it },
                    )
                }
                is Resultat.Echec -> _uiState.update {
                    it.copy(actionEnCoursSurId = null, messageErreur = resultat.message)
                }
            }
        }
    }
}

