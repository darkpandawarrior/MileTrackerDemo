package com.mileway.feature.profile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mileway.core.ui.components.topbar.DepthAwareTopBar
import com.mileway.core.ui.mvi.DefaultEmptyState
import com.mileway.core.ui.mvi.ScreenStateContent
import com.mileway.core.ui.theme.DesignTokens
import com.mileway.core.ui.theme.DesignTokens.NavigationDepth
import com.mileway.core.ui.theme.DesignTokens.StatusColors
import com.mileway.core.ui.theme.dataStyle
import com.mileway.feature.profile.model.AdvanceRecord
import com.mileway.feature.profile.model.AdvanceStatus
import com.mileway.feature.profile.model.ApproverStep
import com.mileway.feature.profile.model.ApproverStepStatus
import com.mileway.feature.profile.model.TimelineEntry
import com.mileway.feature.profile.viewmodel.AdvanceAction
import com.mileway.feature.profile.viewmodel.AdvanceViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

private val MONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private fun formatDate(ms: Long): String =
    Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.currentSystemDefault()).let { ldt ->
        "${ldt.dayOfMonth} ${MONTHS[ldt.monthNumber - 1]} ${ldt.year}"
    }

/**
 * Drill-down detail screen for a single [AdvanceRecord], opened by tapping a card on
 * [AdvanceHistoryScreen]. Shows the hero amount/status, the approver chain, and a request
 * timeline, all sourced from the same stub-backed [AdvanceViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvanceRequestDetailsScreen(
    advanceId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdvanceViewModel = koinViewModel(),
) {
    val ui by viewModel.state.collectAsState()

    LaunchedEffect(advanceId) {
        viewModel.onAction(AdvanceAction.LoadDetail(advanceId))
    }

    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Advance Request",
                subtitle = advanceId,
                depth = NavigationDepth.LEVEL_2,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        ScreenStateContent(
            state = ui.detail,
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding(),
            empty = {
                DefaultEmptyState(
                    title = "Advance not found",
                    subtitle = "This advance request could not be located.",
                )
            },
        ) { record ->
            AdvanceRequestDetailsContent(record = record)
        }
    }
}

@Composable
private fun AdvanceRequestDetailsContent(record: AdvanceRecord) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                horizontal = DesignTokens.Spacing.l,
                vertical = DesignTokens.Spacing.l,
            ),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.l),
    ) {
        item { HeroCard(record) }
        val declineReason = record.declineReason
        if (record.status == AdvanceStatus.REJECTED && !declineReason.isNullOrBlank()) {
            item { DeclineReasonCard(declineReason) }
        }
        if (record.approverChain.isNotEmpty()) {
            item { ApproverChainCard(record.approverChain) }
        }
        if (record.timeline.isNotEmpty()) {
            item { TimelineCard(record.timeline) }
        }
        item { Spacer(Modifier.height(DesignTokens.Spacing.l)) }
    }
}

@Composable
private fun HeroCard(record: AdvanceRecord) {
    val (statusLabel, statusColor) =
        when (record.status) {
            AdvanceStatus.PENDING -> "Pending" to StatusColors.warning
            AdvanceStatus.UNDER_REVIEW -> "Under Review" to StatusColors.info
            AdvanceStatus.APPROVED -> "Approved" to StatusColors.success
            AdvanceStatus.DISBURSED -> "Disbursed" to StatusColors.info
            AdvanceStatus.REJECTED -> "Rejected" to StatusColors.error
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "₹${record.amountRupees.toLong()}",
                style = MaterialTheme.typography.headlineMedium.dataStyle(),
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(DesignTokens.Spacing.s))
            Surface(color = statusColor.copy(alpha = 0.15f), shape = DesignTokens.Shape.chip) {
                Text(
                    statusLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.height(DesignTokens.Spacing.m))
            Text(
                record.purpose,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(DesignTokens.Spacing.xs))
            Text(
                "Requested ${formatDate(record.requestedDateMs)} · Required by ${record.requiredByDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DeclineReasonCard(reason: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        colors = CardDefaults.cardColors(containerColor = StatusColors.error.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l)) {
            Text(
                "Decline Reason",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = StatusColors.error,
            )
            Spacer(Modifier.height(DesignTokens.Spacing.s))
            Text(reason, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ApproverChainCard(steps: List<ApproverStep>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l)) {
            Text("Approver Chain", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(DesignTokens.Spacing.m))
            steps.forEachIndexed { index, step ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = DesignTokens.Spacing.xs))
                ApproverStepRow(step)
            }
        }
    }
}

@Composable
private fun ApproverStepRow(step: ApproverStep) {
    val statusColor =
        when (step.status) {
            ApproverStepStatus.APPROVED -> StatusColors.success
            ApproverStepStatus.REJECTED -> StatusColors.error
            ApproverStepStatus.PENDING -> StatusColors.warning
        }
    val statusLabel =
        when (step.status) {
            ApproverStepStatus.APPROVED -> "Approved"
            ApproverStepStatus.REJECTED -> "Rejected"
            ApproverStepStatus.PENDING -> "Pending"
        }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(color = statusColor, shape = CircleShape),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(step.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(step.stageLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(statusLabel, style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TimelineCard(entries: List<TimelineEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.card),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l)) {
            Text("Timeline", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(DesignTokens.Spacing.m))
            entries.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = DesignTokens.Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(entry.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        formatDate(entry.dateMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
