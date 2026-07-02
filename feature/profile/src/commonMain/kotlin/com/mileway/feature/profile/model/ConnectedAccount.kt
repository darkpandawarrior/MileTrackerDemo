package com.mileway.feature.profile.model

/**
 * PLAN_V22 P6.6: a single third-party integration listed on
 * [com.mileway.feature.profile.ui.screens.ConnectedAccountsScreen] — Mileway's local/offline
 * equivalent of the reference app's cab/passport connect-disconnect integrations list. Toggling
 * [isConnected] never calls a network API (there is no backend to call yet); it only flips the
 * persisted row via [com.mileway.feature.profile.repository.ConnectedAccountsRepository].
 */
data class ConnectedAccount(
    val id: String,
    val providerName: String,
    val category: String,
    val isConnected: Boolean,
)
