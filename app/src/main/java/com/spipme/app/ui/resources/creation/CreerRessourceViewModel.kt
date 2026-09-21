package com.spipme.app.ui.resources.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.security.SessionManager
import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.repository.RessourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class CreerRessourceViewModel @Inject constructor(
    private val ressourceRepository: RessourceRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreerRessourceUiState())
    val uiState: StateFlow<CreerRessourceUiState> = _uiState.asStateFlow()

    fun surChangementType(v: String) = _uiState.update { it.copy(type = v, messageErreur = null) }
    fun surChangementNom(v: String) = _uiState.update { it.copy(nom = v, messageErreur = null) }
    fun surChangementUnite(v: String) = _uiState.update { it.copy(unite = v) }
    fun surChangementNiveauActuel(v: String) = _uiState.update { it.copy(niveauActuel = v, messageErreur = null) }
    fun surChangementSeuilCritique(v: String) = _uiState.update { it.copy(seuilCritique = v, messageErreur = null) }
    fun surChangementSeuilAlerte(v: String) = _uiState.update { it.copy(seuilAlerte = v, messageErreur = null) }
    fun surChangementValeurUnitaire(v: String) = _uiState.update { it.copy(valeurUnitaire = v) }
    fun surChangementEmplacement(v: String) = _uiState.update { it.copy(emplacement = v) }

    private fun versBigDecimalOuNull(texte: String): BigDecimal? =
        texte.trim().takeIf { it.isNotBlank() }?.replace(",", ".")?.toBigDecimalOrNull()

    fun enregistrer() {
        val etat = _uiState.value

        if (etat.nom.isBlank()) {
            _uiState.update { it.copy(messageErreur = "Le nom est obligatoire.") }
            return
        }
        if (etat.type.isBlank()) {
            _uiState.update { it.copy(messageErreur = "La catégorie est obligatoire.") }
            return
        }
        val niveauActuel = versBigDecimalOuNull(etat.niveauActuel)
        if (niveauActuel == null) {
            _uiState.update { it.copy(messageErreur = "Quantité actuelle invalide.") }
            return
        }
        val seuilCritique = if (etat.seuilCritique.isBlank()) null else versBigDecimalOuNull(etat.seuilCritique)
        if (etat.seuilCritique.isNotBlank() && seuilCritique == null) {
            _uiState.update { it.copy(messageErreur = "Seuil critique invalide.") }
            return
        }
        val seuilAlerte = if (etat.seuilAlerte.isBlank()) null else versBigDecimalOuNull(etat.seuilAlerte)
        if (etat.seuilAlerte.isNotBlank() && seuilAlerte == null) {
            _uiState.update { it.copy(messageErreur = "Seuil d'alerte invalide.") }
            return
        }
        if (seuilCritique != null && seuilAlerte != null && seuilCritique > seuilAlerte) {
            _uiState.update { it.copy(messageErreur = "Le seuil critique doit être inférieur ou égal au seuil d'alerte.") }
            return
        }
        val valeurUnitaire = if (etat.valeurUnitaire.isBlank()) null else versBigDecimalOuNull(etat.valeurUnitaire)
        if (etat.valeurUnitaire.isNotBlank() && valeurUnitaire == null) {
            _uiState.update { it.copy(messageErreur = "Valeur unitaire invalide.") }
            return
        }

        viewModelScope.launch {
            val secteurId = sessionManager.secteurActifIdFlow.first()
            if (secteurId == null) {
                _uiState.update { it.copy(messageErreur = "Aucun secteur actif — reconnectez-vous.") }
                return@launch
            }

            _uiState.update { it.copy(enCoursDEnvoi = true, messageErreur = null) }
            val resultat = ressourceRepository.creer(
                secteurId = secteurId,
                type = etat.type,
                nom = etat.nom,
                unite = etat.unite.ifBlank { null },
                valeurUnitaire = valeurUnitaire,
                niveauActuel = niveauActuel,
                seuilCritique = seuilCritique,
                seuilAlerte = seuilAlerte,
                emplacement = etat.emplacement.ifBlank { null },
            )
            when (resultat) {
                is Resultat.Succes -> _uiState.update { it.copy(enCoursDEnvoi = false, creationReussie = true) }
                is Resultat.Echec -> _uiState.update { it.copy(enCoursDEnvoi = false, messageErreur = resultat.message) }
            }
        }
    }
}
