package com.mileway.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mileway.feature.profile.model.ConnectedAccount
import com.mileway.feature.profile.repository.ConnectedAccountsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PLAN_V22 P6.6: state for [com.mileway.feature.profile.ui.screens.ConnectedAccountsScreen] —
 * real Room-backed persistence (via [ConnectedAccountsRepository]) replacing Preferences'
 * previous "Connected Accounts is a demo placeholder." snackbar tap.
 */
data class ConnectedAccountsUiState(val accounts: List<ConnectedAccount> = emptyList())

class ConnectedAccountsViewModel(private val repository: ConnectedAccountsRepository) : ViewModel() {
    private val _state = MutableStateFlow(ConnectedAccountsUiState())
    val state: StateFlow<ConnectedAccountsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { repository.seedIfEmpty() }
        repository.observeAll().onEach { list -> _state.update { it.copy(accounts = list) } }.launchIn(viewModelScope)
    }

    /** Toggles [id]'s connection state — a local flag flip only, never a real network call. */
    fun toggle(
        id: String,
        isConnected: Boolean,
    ) {
        viewModelScope.launch { repository.setConnected(id, isConnected) }
    }
}
