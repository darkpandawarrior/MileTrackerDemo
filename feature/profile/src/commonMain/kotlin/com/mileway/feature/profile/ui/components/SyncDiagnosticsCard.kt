package com.mileway.feature.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mileway.core.ui.theme.DesignTokens
import com.mileway.feature.profile.model.SyncMetrics
import com.mileway.feature.profile.viewmodel.SyncDiagnosticsViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

private val MONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private fun formatLastSync(ms: Long?): String {
    if (ms == null) return "Never"
    val ldt = Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = ldt.hour.toString().padStart(2, '0')
    val minute = ldt.minute.toString().padStart(2, '0')
    return "${ldt.dayOfMonth} ${MONTHS[ldt.monthNumber - 1]}, $hour:$minute"
}

/**
 * PLAN_V22 P6.7: Settings' "Sync Diagnostics" card — locally-generated
 * `locationsSynced`/`eventsSynced`/`failedAttempts`/`lastSyncTime` metrics (see
 * [SyncDiagnosticsRepository][com.mileway.feature.profile.repository.SyncDiagnosticsRepository]),
 * with a "Force Sync" button that re-runs a local upload-simulation job. No network call — Mileway
 * has no backend to sync against yet (see CLAUDE.md "The backend").
 */
@Composable
fun SyncDiagnosticsCard(viewModel: SyncDiagnosticsViewModel = koinViewModel()) {
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    SyncDiagnosticsCardContent(metrics = metrics, onForceSync = viewModel::forceSync)
}

@Composable
internal fun SyncDiagnosticsCardContent(
    metrics: SyncMetrics,
    onForceSync: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.l),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card),
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.l)) {
            Text(
                text = "Sync Diagnostics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(DesignTokens.Spacing.s))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SyncMetricColumn(label = "Locations", value = metrics.locationsSynced.toString())
                SyncMetricColumn(label = "Events", value = metrics.eventsSynced.toString())
                SyncMetricColumn(
                    label = "Failed",
                    value = metrics.failedAttempts.toString(),
                    valueColor =
                        if (metrics.failedAttempts > 0) {
                            DesignTokens.StatusColors.warning
                        } else {
                            DesignTokens.StatusColors.success
                        },
                )
            }
            Spacer(Modifier.height(DesignTokens.Spacing.m))
            Text(
                text = "Last sync: ${formatLastSync(metrics.lastSyncTimeMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(DesignTokens.Spacing.m))
            OutlinedButton(
                onClick = onForceSync,
                enabled = !metrics.isSyncing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (metrics.isSyncing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(text = "Syncing…")
                    }
                } else {
                    Text(text = "Force Sync")
                }
            }
        }
    }
}

@Composable
private fun SyncMetricColumn(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
