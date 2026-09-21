package com.spipme.app.ui.treasury.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CreerTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreerTransactionUiState())
    val uiState: StateFlow<CreerTransactionUiState> = _uiState.asStateFlow()

    fun surChangementType(v: String) = _uiState.update { it.copy(type = v, messageErreur = null) }
    fun surChangementMontant(v: String) = _uiState.update { it.copy(montant = v, messageErreur = null) }
    fun surChangementModePaiement(v: String) = _uiState.update { it.copy(modePaiement = v) }
    fun surChangementDescription(v: String) = _uiState.update { it.copy(description = v) }

    fun enregistrer() {
        val etat = _uiState.value
        val montant = etat.montant.trim().replace(",", ".").toBigDecimalOrNull()
        if (montant == null || montant <= java.math.BigDecimal.ZERO) {
            _uiState.update { it.copy(messageErreur = "Montant invalide.") }
            return
        }

        viewModelScope.launch {
            val secteurId = sessionManager.secteurActifIdFlow.first()
            if (secteurId == null) {
                _uiState.update { it.copy(messageErreur = "Aucun secteur actif — reconnectez-vous.") }
                return@launch
            }

            _uiState.update { it.copy(enCoursDEnvoi = true, messageErreur = null) }
            val resultat = transactionRepository.creer(
                secteurId = secteurId,
                type = etat.type,
                montant = montant,
                modePaiement = etat.modePaiement,
                description = etat.description.ifBlank { null },
                dateTransactionIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
            )
            when (resultat) {
                is Resultat.Succes -> _uiState.update { it.copy(enCoursDEnvoi = false, creationReussie = true) }
                is Resultat.Echec -> _uiState.update { it.copy(enCoursDEnvoi = false, messageErreur = resultat.message) }
            }
        }
    }
}
