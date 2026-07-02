package com.mileway.feature.profile.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mileway.core.ui.components.GridProfileTile
import com.mileway.core.ui.components.ProfileGridItem
import com.mileway.core.ui.components.ProfileItemStatus
import com.mileway.core.ui.components.ProfileSectionHeader
import com.mileway.core.ui.components.topbar.DepthAwareTopBar
import com.mileway.core.ui.theme.DesignTokens
import com.mileway.core.ui.theme.DesignTokens.NavigationDepth
import com.mileway.feature.profile.viewmodel.ProfileAction
import com.mileway.feature.profile.viewmodel.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel
import android.provider.Settings as SystemSettings

/**
 * Preferences: a focused service/system preferences screen pushed from the Account hub.
 *
 * A 2-column grid of large tonal tiles under a single "Settings" section header. Push
 * Notifications/Usage Analytics/P6.5's channel toggles flip a ViewModel-held toggle — the
 * channel toggles persist via [com.mileway.core.data.settings.DemoSettingsRepository]
 * (DataStore); Notification Center and Connected Accounts push their own real destinations;
 * Permissions launches the real system app-details intent; Storage opens
 * [StorageSheet]'s live on-device byte-count readout + clear-cache action — no tile fires a
 * demo-placeholder snackbar anymore (P6.6).
 *
 * Full-screen flow (no bubble bar): the grid's content insets carry the bottom padding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onBack: () -> Unit,
    onOpenNotificationCenter: () -> Unit = {},
    onOpenConnectedAccounts: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showStorageSheet by remember { mutableStateOf(false) }

    // Surface any one-shot preference demo message as a snackbar, then clear it.
    LaunchedEffect(state.preferenceMessage) {
        state.preferenceMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onAction(ProfileAction.ClearPreferenceMessage)
        }
    }

    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Preferences",
                subtitle = "Manage your settings",
                depth = NavigationDepth.LEVEL_1,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val prefs = state.preferences
        val channels = state.notificationChannels
        val items =
            listOf(
                ProfileGridItem(
                    id = "push_notifications",
                    title = "Push Notifications",
                    subtitle = if (prefs.pushNotifications) "Enabled" else "Disabled",
                    icon = Icons.Default.Notifications,
                    status = if (prefs.pushNotifications) ProfileItemStatus.COMPLETE else ProfileItemStatus.INCOMPLETE,
                    action = { viewModel.onAction(ProfileAction.TogglePushNotifications) },
                ),
                ProfileGridItem(
                    id = "usage_analytics",
                    title = "Usage Analytics",
                    subtitle = if (prefs.usageAnalytics) "Enabled" else "Disabled",
                    icon = Icons.Default.BarChart,
                    status = if (prefs.usageAnalytics) ProfileItemStatus.COMPLETE else ProfileItemStatus.INCOMPLETE,
                    action = { viewModel.onAction(ProfileAction.ToggleUsageAnalytics) },
                ),
                // P6.6: routes to P6.5's real NotificationCentreScreen instead of a placeholder snackbar.
                ProfileGridItem(
                    id = "notification_center",
                    title = "Notification Center",
                    subtitle = "WhatsApp, Slack",
                    icon = Icons.Default.NotificationsActive,
                    status = ProfileItemStatus.COMPLETE,
                    action = onOpenNotificationCenter,
                ),
                // P6.5: Notification Center channel toggles — Mileway's local/offline equivalent
                // of connect/disconnect switches, DataStore-backed so state survives restart.
                ProfileGridItem(
                    id = "channel_push",
                    title = "Push Channel",
                    subtitle = if (channels.pushEnabled) "Enabled" else "Disabled",
                    icon = Icons.Default.Notifications,
                    status = if (channels.pushEnabled) ProfileItemStatus.COMPLETE else ProfileItemStatus.INCOMPLETE,
                    action = { viewModel.onAction(ProfileAction.TogglePushChannel) },
                ),
                ProfileGridItem(
                    id = "channel_whatsapp",
                    title = "WhatsApp Channel",
                    subtitle = if (channels.whatsappEnabled) "Enabled" else "Disabled",
                    icon = Icons.Default.NotificationsActive,
                    status = if (channels.whatsappEnabled) ProfileItemStatus.COMPLETE else ProfileItemStatus.INCOMPLETE,
                    action = { viewModel.onAction(ProfileAction.ToggleWhatsappChannel) },
                ),
                ProfileGridItem(
                    id = "channel_slack",
                    title = "Slack Channel",
                    subtitle = if (channels.slackEnabled) "Enabled" else "Disabled",
                    icon = Icons.Default.Link,
                    status = if (channels.slackEnabled) ProfileItemStatus.COMPLETE else ProfileItemStatus.INCOMPLETE,
                    action = { viewModel.onAction(ProfileAction.ToggleSlackChannel) },
                ),
                // P6.6: routes to the new Room-backed ConnectedAccountsScreen instead of a placeholder snackbar.
                ProfileGridItem(
                    id = "connected_accounts",
                    title = "Connected Accounts",
                    subtitle = "Cabs, Passport",
                    icon = Icons.Default.Link,
                    status = ProfileItemStatus.COMPLETE,
                    action = onOpenConnectedAccounts,
                ),
                // P6.6: launches the real system app-details intent instead of a placeholder snackbar.
                ProfileGridItem(
                    id = "permissions",
                    title = "Permissions",
                    subtitle = "System settings",
                    icon = Icons.Default.Security,
                    status = ProfileItemStatus.COMPLETE,
                    action = {
                        val intent =
                            Intent(SystemSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        context.startActivity(intent)
                    },
                ),
                // P6.6: opens StorageSheet's live byte-count readout + clear-cache action instead of
                // a placeholder snackbar.
                ProfileGridItem(
                    id = "storage",
                    title = "Storage",
                    subtitle = "Manage data",
                    icon = Icons.Default.Storage,
                    status = ProfileItemStatus.COMPLETE,
                    action = { showStorageSheet = true },
                ),
            )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentPadding = PaddingValues(DesignTokens.Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
        ) {
            item(span = { GridItemSpan(2) }) {
                ProfileSectionHeader(
                    title = "Settings",
                    itemCount = items.size,
                    icon = Icons.Default.Settings,
                )
            }
            items(items, key = { it.id }) { item ->
                GridProfileTile(item = item)
            }
        }
    }

    if (showStorageSheet) {
        StorageSheet(onDismiss = { showStorageSheet = false })
    }
}
