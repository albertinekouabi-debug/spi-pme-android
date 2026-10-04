package com.spipme.app.ui.auth.motdepasseoublie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Écran "Mot de passe oublié" (absent à l'audit initial — lien non câblé
 * dans LoginScreen). Deux étapes dans un seul écran, comme la maquette :
 *
 *  1. DEMANDE   : saisie de l'email, POST /auth/password-reset/request.
 *  2. CONFIRMATION : saisie du token reçu par email + nouveau mot de passe,
 *     POST /auth/password-reset/confirm.
 *
 * Le serveur répond 200 que l'email existe ou non (anti-énumération) : le
 * message affiché à l'étape 1 est donc IDENTIQUE dans les deux cas, sans
 * jamais indiquer si un compte a été trouvé.
 */
@HiltViewModel
class MotDePasseOublieViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotDePasseOublieUiState())
    val uiState: StateFlow<MotDePasseOublieUiState> = _uiState.asStateFlow()

    fun surChangementEmail(valeur: String) {
        _uiState.update { it.copy(email = valeur, messageErreur = null) }
    }

    fun surChangementToken(valeur: String) {
        _uiState.update { it.copy(token = valeur, messageErreur = null) }
    }

    fun surChangementNouveauMotDePasse(valeur: String) {
        _uiState.update { it.copy(nouveauMotDePasse = valeur, messageErreur = null) }
    }

    fun demanderReinitialisation() {
        val email = _uiState.value.email
        if (email.isBlank()) {
            _uiState.update { it.copy(messageErreur = "Veuillez renseigner votre email.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(enCours = true, messageErreur = null) }
            when (val resultat = authRepository.demanderReinitialisationMotDePasse(email)) {
                is Resultat.Succes -> _uiState.update {
                    it.copy(enCours = false, etape = EtapeMotDePasseOublie.CONFIRMATION)
                }
                is Resultat.Echec -> _uiState.update { it.copy(enCours = false, messageErreur = resultat.message) }
            }
        }
    }

    fun confirmerReinitialisation() {
        val etat = _uiState.value
        if (etat.token.isBlank() || etat.nouveauMotDePasse.length < 8) {
            _uiState.update {
                it.copy(messageErreur = "Renseignez le code reçu par email et un mot de passe d'au moins 8 caractères.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(enCours = true, messageErreur = null) }
            when (val resultat = authRepository.confirmerReinitialisationMotDePasse(etat.token, etat.nouveauMotDePasse)) {
                is Resultat.Succes -> _uiState.update { it.copy(enCours = false, reinitialisationReussie = true) }
                is Resultat.Echec -> _uiState.update { it.copy(enCours = false, messageErreur = resultat.message) }
            }
        }
    }

    /** Permet à l'utilisateur de revenir en arrière s'il n'a pas reçu l'email. */
    fun revenirALaDemande() {
        _uiState.update { it.copy(etape = EtapeMotDePasseOublie.DEMANDE, messageErreur = null) }
    }
}
