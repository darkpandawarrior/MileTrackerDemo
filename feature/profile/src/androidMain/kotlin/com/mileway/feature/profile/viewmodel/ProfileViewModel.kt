package com.mileway.feature.profile.viewmodel

import androidx.lifecycle.viewModelScope
import com.mileway.core.ui.mvi.BaseViewModel
import com.mileway.core.ui.theme.AccentPalette
import com.mileway.core.ui.theme.AppLanguage
import com.mileway.core.ui.theme.ExperimentalFlags
import com.mileway.core.ui.theme.MilewayThemeVariant
import com.mileway.core.ui.theme.ThemeController
import com.mileway.feature.profile.model.AccountAnalyticsSnapshot
import com.mileway.feature.profile.model.ProfileUiState
import com.mileway.feature.profile.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileAction {
    data class SwitchAccount(val id: String) : ProfileAction

    data object OpenSessionsDialog : ProfileAction

    data object DismissSessionsDialog : ProfileAction

    data object TogglePushNotifications : ProfileAction

    data object ToggleUsageAnalytics : ProfileAction

    data class RaisePreferenceMessage(val message: String) : ProfileAction

    data object ClearPreferenceMessage : ProfileAction

    /** P1.3: adds a new switchable persona (never active on creation). */
    data class AddDemoAccount(val displayName: String, val employeeCode: String, val organization: String) : ProfileAction

    /** P1.3: removes a persona; a no-op + [RaisePreferenceMessage] when it's active or the last remaining one. */
    data class RemoveDemoAccount(val accountId: String) : ProfileAction

    /** P1.3: opens [AccountDetailsSheet][com.mileway.feature.profile.ui.screens.AccountDetailsSheet] for a persona. */
    data class ViewAccountDetails(val accountId: String) : ProfileAction

    /** P1.3: dismisses the details sheet opened by [ViewAccountDetails]. */
    data object DismissAccountDetails : ProfileAction
}

/** No one-shot effects; preference messages live in state. Present to satisfy the MVI contract. */
sealed interface ProfileEffect

/**
 * Backs the whole account suite (Account hub, Profile Details, Preferences, Settings/Help).
 * Its own state ([ProfileUiState]) is mutated only through [onAction]; theme-related reactive
 * state and setters delegate to [ThemeController] (a dedicated controller, not this VM's state),
 * so they remain passthrough properties/methods.
 */
class ProfileViewModel(
    private val repository: ProfileRepository,
    private val themeController: ThemeController,
) : BaseViewModel<ProfileUiState, ProfileEffect, ProfileAction>(
        repository.accounts().let { acc ->
            ProfileUiState(
                header = repository.header(),
                profile = repository.richProfile(),
                completion = repository.completion(),
                sessions = repository.sessions(),
                accounts = acc,
                selectedAccountId = acc.firstOrNull()?.id.orEmpty(),
                analytics = AccountAnalyticsSnapshot.demo(),
            )
        },
    ) {
    /** Backwards-compatible alias; screens read [state]. */
    val uiState: StateFlow<ProfileUiState> = state

    init {
        // P1.2: `accounts()` above seeds the initial render synchronously (unchanged shape);
        // once the Room-backed store is seeded, switch the list to the live DAO-backed Flow so
        // it stays observably in sync with adds/removes (P1.3) instead of a one-shot snapshot.
        viewModelScope.launch {
            repository.seedAccountsIfEmpty()
            repository.observeAccounts().collect { accounts ->
                setState {
                    val stillSelected = accounts.any { it.id == selectedAccountId }
                    copy(
                        accounts = accounts,
                        selectedAccountId =
                            if (stillSelected) selectedAccountId else accounts.firstOrNull()?.id.orEmpty(),
                    )
                }
            }
        }
    }

    override fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.SwitchAccount -> setState { copy(selectedAccountId = action.id) }
            ProfileAction.OpenSessionsDialog -> setState { copy(showSessionsDialog = true) }
            ProfileAction.DismissSessionsDialog -> setState { copy(showSessionsDialog = false) }
            ProfileAction.TogglePushNotifications ->
                setState { copy(preferences = preferences.copy(pushNotifications = !preferences.pushNotifications)) }
            ProfileAction.ToggleUsageAnalytics ->
                setState { copy(preferences = preferences.copy(usageAnalytics = !preferences.usageAnalytics)) }
            is ProfileAction.RaisePreferenceMessage -> setState { copy(preferenceMessage = action.message) }
            ProfileAction.ClearPreferenceMessage -> setState { copy(preferenceMessage = null) }
            is ProfileAction.AddDemoAccount -> addDemoAccount(action)
            is ProfileAction.RemoveDemoAccount -> removeDemoAccount(action.accountId)
            is ProfileAction.ViewAccountDetails -> viewAccountDetails(action.accountId)
            ProfileAction.DismissAccountDetails -> setState { copy(accountDetailsSheet = null) }
        }
    }

    private fun addDemoAccount(action: ProfileAction.AddDemoAccount) {
        viewModelScope.launch {
            repository.addAccount(action.displayName, action.employeeCode, action.organization)
        }
    }

    /**
     * Guard mirrors the reference app's `AccountCard` disabled-delete-on-active rule plus the implicit
     * "can't remove your only account" invariant: removing the currently-active persona or the
     * last remaining one is a no-op with a snackbar instead of a crash-prone empty account list.
     */
    private fun removeDemoAccount(accountId: String) {
        val accounts = currentState.accounts
        val isActive = accountId == currentState.selectedAccountId
        val isLast = accounts.size <= 1
        when {
            isActive -> setState { copy(preferenceMessage = "Switch to another persona before removing this one") }
            isLast -> setState { copy(preferenceMessage = "You need at least one account") }
            else -> viewModelScope.launch { repository.removeAccount(accountId) }
        }
    }

    private fun viewAccountDetails(accountId: String) {
        val account = currentState.accounts.find { it.id == accountId } ?: return
        setState { copy(accountDetailsSheet = account) }
    }

    // ── Theme delegations (ThemeController owns this reactive state) ───────────

    /** `null` = follow system, `true` = force dark, `false` = force light. */
    val darkThemeOverride: StateFlow<Boolean?> = themeController.darkThemeOverride

    /** Currently selected curated theme (Design Language v2; default Matrix). */
    val milewayTheme: StateFlow<MilewayThemeVariant> = themeController.milewayTheme

    val accentPalette: StateFlow<AccentPalette> = themeController.accentPalette

    val customSeedHex: StateFlow<String> = themeController.customSeedHex

    val useSystemColors: StateFlow<Boolean> = themeController.useSystemColors

    val paletteStyle: StateFlow<String> = themeController.paletteStyle

    val language: StateFlow<AppLanguage> = themeController.language

    val experimentalFlags: StateFlow<ExperimentalFlags> = themeController.experimentalFlags

    private val _useMiles = MutableStateFlow(true)
    val useMiles: StateFlow<Boolean> = _useMiles.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleUnits() {
        _useMiles.value = !_useMiles.value
    }

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun setDarkTheme(dark: Boolean?) = themeController.set(dark)

    /** Select a curated Design Language v2 theme (Matrix / Amoled / Ion / Daybreak). */
    fun setMilewayTheme(theme: MilewayThemeVariant) = themeController.setMilewayTheme(theme)

    fun setPalette(palette: AccentPalette) = themeController.setPalette(palette)

    fun setCustomSeed(hex: String) = themeController.setCustomSeed(hex)

    fun setUseSystemColors(enabled: Boolean) = themeController.setUseSystemColors(enabled)

    fun setPaletteStyle(style: String) = themeController.setPaletteStyle(style)

    fun setLanguage(language: AppLanguage) = themeController.setLanguage(language)

    fun toggleBatteryAwareTracking() {
        val current = themeController.experimentalFlags.value
        themeController.updateExperimentalFlags(current.copy(batteryAwareTracking = !current.batteryAwareTracking))
    }

    fun toggleLowEndDeviceTuning() {
        val current = themeController.experimentalFlags.value
        themeController.updateExperimentalFlags(current.copy(lowEndDeviceTuning = !current.lowEndDeviceTuning))
    }

    fun toggleAggressiveGpsFilter() {
        val current = themeController.experimentalFlags.value
        themeController.updateExperimentalFlags(current.copy(aggressiveGpsFilter = !current.aggressiveGpsFilter))
    }

    fun resetCustomization() = themeController.resetCustomization()
}
