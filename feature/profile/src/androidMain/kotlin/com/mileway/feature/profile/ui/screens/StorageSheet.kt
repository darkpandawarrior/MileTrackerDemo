package com.mileway.feature.profile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mileway.core.ui.components.sheet.ActionConfirmationBottomSheet
import com.mileway.core.ui.components.sheet.ActionConfirmationToneType
import com.mileway.core.ui.theme.DesignTokens
import com.mileway.feature.profile.viewmodel.StorageViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * PLAN_V22 P6.6: Preferences' "Storage" tile's real on-device cache-size readout + working
 * clear-cache action — replaces the tile's previous
 * `ProfileAction.RaisePreferenceMessage("Manage local data in the full app.")` snackbar tap. Own
 * Matrix/terminal bottom-sheet layout matching [com.mileway.feature.profile.ui.screens
 * .AccountDetailsSheet]'s idiom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageSheet(
    onDismiss: () -> Unit,
    viewModel: StorageViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ActionConfirmationBottomSheet(
        title = "Storage",
        description = "Manage the space this app uses on your device.",
        confirmLabel = if (state.isClearing) "Clearing…" else "Clear Cache",
        dismissLabel = "Close",
        icon = Icons.Default.Storage,
        tone = ActionConfirmationToneType.Info,
        onConfirm = { viewModel.clearCache() },
        onDismiss = onDismiss,
    ) {
        Surface(
            shape = DesignTokens.Shape.roundedMd,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.Spacing.l),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
            ) {
                StorageRow(label = "Database", value = state.databaseLabel)
                StorageRow(label = "Cache", value = state.cacheLabel)
                StorageRow(label = "Total", value = state.totalLabel)
            }
        }
        if (state.isClearing) {
            CircularProgressIndicator(modifier = Modifier.padding(top = DesignTokens.Spacing.m).size(20.dp))
        }
        if (state.didClear) {
            Text(
                text = "Cache cleared",
                style = MaterialTheme.typography.labelMedium,
                color = DesignTokens.StatusColors.success,
                modifier = Modifier.padding(top = DesignTokens.Spacing.s),
            )
        }
    }
}

@Composable
private fun StorageRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
