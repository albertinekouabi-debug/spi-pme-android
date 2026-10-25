package com.spipme.app.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun surChangementIdentifiant(valeur: String) {
        _uiState.update { it.copy(identifiant = valeur, messageErreur = null) }
    }

    fun surChangementMotDePasse(valeur: String) {
        _uiState.update { it.copy(motDePasse = valeur, messageErreur = null) }
    }

    fun seConnecter() {
        val etat = _uiState.value
        if (etat.enCoursDeConnexion || etat.connexionReussie) return

        val identifiant = etat.identifiant.trim()
        if (identifiant.isBlank() || etat.motDePasse.isBlank()) {
            _uiState.update {
                it.copy(messageErreur = "Veuillez renseigner votre identifiant et votre mot de passe.")
            }
            return
        }

        _uiState.update { it.copy(enCoursDeConnexion = true, messageErreur = null) }
        viewModelScope.launch {
            try {
                when (val resultat = authRepository.connexion(identifiant, etat.motDePasse)) {
                    is Resultat.Succes -> _uiState.update {
                        it.copy(enCoursDeConnexion = false, connexionReussie = true, messageErreur = null)
                    }
                    is Resultat.Echec -> _uiState.update {
                        it.copy(enCoursDeConnexion = false, messageErreur = resultat.message)
                    }
                }
            } catch (annulation: CancellationException) {
                throw annulation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        enCoursDeConnexion = false,
                        messageErreur = "La connexion n'a pas abouti. Vérifiez le réseau puis réessayez.",
                    )
                }
            }
        }
    }
}