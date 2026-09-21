package com.spipme.app.ui.auth.register

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

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun surChangementNomUtilisateur(valeur: String) {
        _uiState.update { it.copy(nomUtilisateur = valeur, messageErreur = null) }
    }

    fun surChangementEmail(valeur: String) {
        _uiState.update { it.copy(email = valeur, messageErreur = null) }
    }

    fun surChangementMotDePasse(valeur: String) {
        _uiState.update { it.copy(motDePasse = valeur, messageErreur = null) }
    }

    fun surChangementConfirmationMotDePasse(valeur: String) {
        _uiState.update { it.copy(confirmationMotDePasse = valeur, messageErreur = null) }
    }

    fun surChangementNomComplet(valeur: String) {
        _uiState.update { it.copy(nomComplet = valeur, messageErreur = null) }
    }

    fun surChangementCodeInvitation(valeur: String) {
        _uiState.update { it.copy(codeInvitation = valeur, messageErreur = null) }
    }

    fun sInscrire() {
        val etat = _uiState.value
        val erreurValidation = validerLocalement(etat)
        if (erreurValidation != null) {
            _uiState.update { it.copy(messageErreur = erreurValidation) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(enCoursDInscription = true, messageErreur = null) }
            val resultat = authRepository.inscription(
                nomUtilisateur = etat.nomUtilisateur.trim(),
                email = etat.email.trim(),
                motDePasse = etat.motDePasse,
                codeInvitation = etat.codeInvitation.trim(),
                nomComplet = etat.nomComplet.trim().ifBlank { null },
                telephone = null,
            )
            when (resultat) {
                is Resultat.Succes -> _uiState.update { it.copy(enCoursDInscription = false, inscriptionReussie = true) }
                is Resultat.Echec -> _uiState.update { it.copy(enCoursDInscription = false, messageErreur = resultat.message) }
            }
        }
    }

    /**
     * Vérifications rapides côté client (champs vides, longueur mini, confirmation).
     * La politique de mot de passe complète (AUTH_PASSWORD_VALIDATORS) et la
     * validité du code d'invitation restent arbitrées côté serveur — pas
     * dupliquées ici pour éviter une double source de vérité.
     */
    private fun validerLocalement(etat: RegisterUiState): String? = when {
        etat.nomUtilisateur.isBlank() -> "Veuillez choisir un nom d'utilisateur."
        etat.email.isBlank() || !etat.email.contains("@") -> "Veuillez saisir un email valide."
        etat.codeInvitation.isBlank() -> "Le code d'invitation fourni par votre entreprise est requis."
        etat.motDePasse.length < 8 -> "Le mot de passe doit contenir au moins 8 caractères."
        etat.motDePasse != etat.confirmationMotDePasse -> "Les deux mots de passe ne correspondent pas."
        else -> null
    }
}
