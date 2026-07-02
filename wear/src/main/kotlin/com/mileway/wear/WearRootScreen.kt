package com.mileway.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.mileway.wear.theme.WearMilewayTheme
import kotlin.math.roundToInt
import org.koin.compose.viewmodel.koinViewModel

/**
 * P2.4: the Wear app's dashboard — today/week distance, a tracking-status pill, and the week-goal
 * progress ring, all rendered from the shared [com.mileway.core.data.model.display.SurfaceSnapshot]
 * via [WearViewModel]/[WearPresentation]. Single-activity, state `when` over [WearRootUiState]
 * (biciradar pattern) — there's exactly one state shape today; P2.5 adds trip-list/detail states
 * to this same `when`.
 */
@Composable
fun WearRootScreen(viewModel: WearViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WearMilewayTheme {
        AppScaffold {
            val listState = rememberScalingLazyListState()
            ScreenScaffold(scrollState = listState) {
                WearDashboard(uiState = uiState, listState = listState)
            }
        }
    }
}

@Composable
private fun WearDashboard(
    uiState: WearRootUiState,
    listState: ScalingLazyListState,
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
    ) {
        item { DashboardHeader() }
        item { TrackingPill(isTracking = uiState.isTracking) }
        item { DistanceCard(label = "TODAY", km = uiState.todayDistanceKm) }
        item { DistanceCard(label = "WEEK", km = uiState.weekDistanceKm) }
        item { WeekGoalCard(uiState = uiState) }
    }
}

@Composable
private fun DashboardHeader() {
    Text(
        text = "Mileway",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

/** A non-interactive info panel — Wear's `Card` composable is click-only, so a plain surface
 * container is used for read-only dashboard tiles instead (no fake `onClick = {}` semantics). */
@Composable
private fun InfoPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(CARD_CORNER_RADIUS_DP.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(CARD_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CARD_SPACING_DP.dp),
    ) {
        content()
    }
}

@Composable
private fun TrackingPill(isTracking: Boolean) {
    InfoPanel(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isTracking) "TRACKING" else "IDLE",
            style = MaterialTheme.typography.labelMedium,
            color = if (isTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DistanceCard(
    label: String,
    km: Double,
) {
    InfoPanel(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(
            text = "%.1f km".format(km),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun WeekGoalCard(uiState: WearRootUiState) {
    InfoPanel(modifier = Modifier.fillMaxWidth()) {
        Text(text = "WEEK GOAL", style = MaterialTheme.typography.labelSmall)
        CircularProgressIndicator(progress = { uiState.weekGoalProgress })
        Text(
            text = "${(uiState.weekGoalProgress * PERCENT_SCALE).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private const val CARD_CORNER_RADIUS_DP = 12
private const val CARD_PADDING_DP = 8
private const val CARD_SPACING_DP = 2
private const val PERCENT_SCALE = 100

// UnusedPrivateMember: only called by the Compose/Android Studio preview renderer via reflection
// on the @Preview annotation, never from Kotlin call sites — see WearMilewayThemePreview's identical
// suppression in WearMilewayTheme.kt for the same known detekt false positive.
@Suppress("DEPRECATION", "UnusedPrivateMember")
@Preview(name = "Wear dashboard", device = Devices.WEAR_OS_SMALL_ROUND, showBackground = true)
@Composable
private fun WearDashboardPreview() {
    WearMilewayTheme {
        AppScaffold {
            val listState = rememberScalingLazyListState()
            ScreenScaffold(scrollState = listState) {
                WearDashboard(
                    uiState =
                        WearRootUiState(
                            todayDistanceKm = 12.4,
                            weekDistanceKm = 58.7,
                            isTracking = true,
                            weekGoalKm = 100.0,
                            weekGoalProgress = 0.587f,
                        ),
                    listState = listState,
                )
            }
        }
    }
}
