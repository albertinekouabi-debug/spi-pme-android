package com.spipme.app.core.notifications

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Passerelle entre [NotificationBadgeManager] (état partagé, un seul par
 * session applicative) et Compose (qui n'injecte que des `@HiltViewModel`).
 * Ne détient aucun état propre : délègue tout au manager.
 */
@HiltViewModel
class NotificationBadgeViewModel @Inject constructor(
    manager: NotificationBadgeManager,
) : ViewModel() {
    val compte: StateFlow<Int> = manager.compte
}
