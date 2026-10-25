package com.spipme.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spipme.app.domain.repository.AuthRepository
import com.spipme.app.ui.navigation.Ecran
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    private val _ecranDepart = MutableStateFlow<String?>(null)
    val ecranDepart: StateFlow<String?> = _ecranDepart.asStateFlow()

    init {
        viewModelScope.launch {
            val estConnecte = authRepository.estConnecteFlow.first()
            _ecranDepart.value = if (estConnecte) Ecran.Accueil.route else Ecran.Connexion.route
        }
    }
}
