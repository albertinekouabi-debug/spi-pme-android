package com.spipme.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Utilisateur
import com.spipme.app.domain.repository.AuthRepository
import com.spipme.app.domain.repository.MoiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val chargement: Boolean = true,
    val utilisateur: Utilisateur? = null,
    val enEdition: Boolean = false,
    val nomComplet: String = "",
    val telephone: String = "",
    val enregistrementEnCours: Boolean = false,
    val ancienMotDePasse: String = "",
    val nouveauMotDePasse: String = "",
    val confirmationNouveauMotDePasse: String = "",
    val changementMotDePasseEnCours: Boolean = false,
    val messageErreur: String? = null,
    val messageSucces: String? = null,
    val deconnecte: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val moiRepository: MoiRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { charger() }

    fun rafraichir() = charger()

    private fun charger() {
        viewModelScope.launch {
            _uiState.update { it.copy(chargement = true, messageErreur = null) }
            when (val resultat = moiRepository.obtenirProfil()) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(
                        chargement = false,
                        utilisateur = resultat.donnees,
                        nomComplet = resultat.donnees.nomComplet.orEmpty(),
                        telephone = resultat.donnees.telephone.orEmpty(),
                    )
                }
                is Resultat.Echec -> _uiState.update { it.copy(chargement = false, messageErreur = resultat.message) }
            }
        }
    }

    fun activerEdition() = _uiState.update { it.copy(enEdition = true) }

    fun annulerEdition() {
        val utilisateur = _uiState.value.utilisateur
        _uiState.update {
            it.copy(
                enEdition = false,
                nomComplet = utilisateur?.nomComplet.orEmpty(),
                telephone = utilisateur?.telephone.orEmpty(),
            )
        }
    }

    fun surChangementNomComplet(valeur: String) = _uiState.update { it.copy(nomComplet = valeur) }
    fun surChangementTelephone(valeur: String) = _uiState.update { it.copy(telephone = valeur) }

    /** Seuls nom_complet/telephone sont envoyÃ©s â€” le rÃ´le, le secteur et le statut ne sont jamais modifiables ici. */
    fun enregistrer() {
        viewModelScope.launch {
            _uiState.update { it.copy(enregistrementEnCours = true, messageErreur = null) }
            val etat = _uiState.value
            when (val resultat = moiRepository.mettreAJourProfil(etat.nomComplet.trim(), etat.telephone.trim())) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(
                        enregistrementEnCours = false,
                        enEdition = false,
                        utilisateur = resultat.donnees,
                        messageSucces = "Profil mis Ã  jour.",
                    )
                }
                is Resultat.Echec -> _uiState.update {
                    it.copy(enregistrementEnCours = false, messageErreur = resultat.message)
                }
            }
        }
    }

    fun surChangementAncienMotDePasse(v: String) = _uiState.update { it.copy(ancienMotDePasse = v) }
    fun surChangementNouveauMotDePasse(v: String) = _uiState.update { it.copy(nouveauMotDePasse = v) }
    fun surChangementConfirmationNouveauMotDePasse(v: String) = _uiState.update { it.copy(confirmationNouveauMotDePasse = v) }

    fun changerMotDePasse() {
        val etat = _uiState.value
        if (etat.nouveauMotDePasse.length < 8) {
            _uiState.update { it.copy(messageErreur = "Le nouveau mot de passe doit contenir au moins 8 caractÃ¨res.") }
            return
        }
        if (etat.nouveauMotDePasse != etat.confirmationNouveauMotDePasse) {
            _uiState.update { it.copy(messageErreur = "Les deux mots de passe ne correspondent pas.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(changementMotDePasseEnCours = true, messageErreur = null) }
            when (val resultat = moiRepository.changerMotDePasse(etat.ancienMotDePasse, etat.nouveauMotDePasse)) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(
                        changementMotDePasseEnCours = false,
                        ancienMotDePasse = "",
                        nouveauMotDePasse = "",
                        confirmationNouveauMotDePasse = "",
                        messageSucces = "Mot de passe modifiÃ©.",
                    )
                }
                is Resultat.Echec -> _uiState.update {
                    it.copy(changementMotDePasseEnCours = false, messageErreur = resultat.message)
                }
            }
        }
    }

    fun messageConsomme() = _uiState.update { it.copy(messageErreur = null, messageSucces = null) }

    fun seDeconnecter() {
        viewModelScope.launch {
            authRepository.deconnexion()
            _uiState.update { it.copy(deconnecte = true) }
        }
    }
}

