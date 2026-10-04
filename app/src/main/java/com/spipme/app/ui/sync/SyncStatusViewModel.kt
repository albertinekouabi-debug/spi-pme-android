package com.spipme.app.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.core.sync.EtatSynchronisation
import com.spipme.app.core.sync.OperationEnAttente
import com.spipme.app.core.sync.android.SynchronisationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val synchronisation: SynchronisationRepository,
) : ViewModel() {

    val etat: StateFlow<EtatSynchronisation> =
        synchronisation.etat.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatSynchronisation())

    val aTraiter: StateFlow<List<OperationEnAttente>> =
        synchronisation.aTraiter.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _dialogueOuvert = MutableStateFlow(false)
    val dialogueOuvert: StateFlow<Boolean> = _dialogueOuvert.asStateFlow()

    fun ouvrir() { _dialogueOuvert.value = true }
    fun fermer() { _dialogueOuvert.value = false }

    fun relancer(id: Long) = viewModelScope.launch { synchronisation.relancer(id) }
    fun abandonner(id: Long) = viewModelScope.launch { synchronisation.abandonner(id) }
    fun garderMaVersion(id: Long) = viewModelScope.launch { synchronisation.garderMaVersion(id) }
}
