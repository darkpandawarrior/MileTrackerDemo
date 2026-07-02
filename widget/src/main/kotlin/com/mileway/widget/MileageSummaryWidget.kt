package com.mileway.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mileway.core.data.watch.SnapshotCache
import com.mileway.core.data.watch.WatchSyncPayload
import com.mileway.feature.tracking.watch.WatchFacade
import kotlin.math.round
import org.koin.mp.KoinPlatform

// Fixed palette (the app's matrix-dark surface + green accent) — plain Glance colors keep the widget free
// of the Material-You/glance-material3 surface so it renders identically across hosts.
private val SurfaceColor = Color(0xFF0E1116)
private val AccentColor = Color(0xFF4ADE80)
private val OnSurfaceColor = Color(0xFFE6E6E6)

/**
 * P6.2: state a widget renders — a pure projection of [WatchSyncPayload] (the same wire shape
 * [SnapshotCache] persists) down to the strings/flags [MileageSummaryContent] lays out. Kept
 * separate from the payload itself (rather than rendering [WatchSyncPayload] fields directly) so
 * the render layer never needs to know the payload's field names, and so the mapping is
 * unit-testable without a Glance host (see `WidgetUiModelTest`).
 */
data class WidgetUiModel(
    val todayLabel: String,
    val weekLabel: String,
    val statusLabel: String?,
    val isTracking: Boolean,
)

/** Pure state->widget mapper (P6.2 acceptance). No cache/Koin/Glance dependency, trivially testable. */
fun WatchSyncPayload.toWidgetUiModel(): WidgetUiModel =
    WidgetUiModel(
        todayLabel = "Today   ${format1(todayKm)} km",
        weekLabel = "Week    ${format1(weekKm)} km · $tripCount trips",
        statusLabel =
            when {
                isTracking && isPaused -> "‖ Paused"
                isTracking -> "● Tracking now"
                else -> null
            },
        isTracking = isTracking,
    )

private const val ONE_DECIMAL_SCALE = 10.0

private fun format1(value: Double): String {
    val scaled = round(value * ONE_DECIMAL_SCALE) / ONE_DECIMAL_SCALE
    return scaled.toString()
}

/**
 * P6.2: a home-screen [GlanceAppWidget] summarising today's/this-week's mileage, plus an
 * interactive Start/Stop button. Reads [SnapshotCache] (P6.1) rather than opening the Room
 * database directly (widgets are re-launched cold on every timeline refresh — see
 * [SnapshotCache]'s doc comment for why touching Room from here is the anti-pattern P6.1 replaces).
 * Resolves its dependencies via `KoinPlatform.getKoin()` — same pattern
 * `WearTrackingCommandService`/`MileageTileService` use for framework-instantiated Android
 * components Koin cannot constructor-inject — since a home-screen widget runs in-process on
 * Android (unlike an iOS WidgetKit extension), the app's already-started Koin graph is always
 * reachable here.
 */
class MileageSummaryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val payload = KoinPlatform.getKoin().getOrNull<SnapshotCache>()?.read() ?: WatchSyncPayload()
        provideContent {
            MileageSummaryContent(payload.toWidgetUiModel())
        }
    }
}

/**
 * Stateless render of a [WidgetUiModel]. Public so the Glance render test can drive it directly
 * with a fixed model (no cache/Koin), matching the "test trivially" contract of the shared model.
 */
@Composable
fun MileageSummaryContent(model: WidgetUiModel) {
    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(SurfaceColor)
                .cornerRadius(16.dp)
                .padding(16.dp),
    ) {
        Text(
            text = "Mileway",
            style = TextStyle(color = ColorProvider(AccentColor), fontWeight = FontWeight.Bold, fontSize = 16.sp),
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = model.todayLabel,
            style = TextStyle(color = ColorProvider(OnSurfaceColor), fontSize = 14.sp),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = model.weekLabel,
            style = TextStyle(color = ColorProvider(OnSurfaceColor), fontSize = 14.sp),
        )
        if (model.statusLabel != null) {
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = model.statusLabel,
                style = TextStyle(color = ColorProvider(AccentColor), fontWeight = FontWeight.Medium, fontSize = 13.sp),
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = if (model.isTracking) "■ Stop" else "▶ Start",
            style = TextStyle(color = ColorProvider(AccentColor), fontWeight = FontWeight.Bold, fontSize = 14.sp),
            modifier =
                GlanceModifier
                    .clickable(
                        actionRunCallback<ToggleTrackingAction>(
                            actionParametersOf(IsTrackingKey.to(model.isTracking)),
                        ),
                    )
                    .semantics {
                        contentDescription = if (model.isTracking) "Stop tracking" else "Start tracking"
                    },
        )
    }
}

private val IsTrackingKey = ActionParameters.Key<Boolean>("is_tracking")

/**
 * The widget's quick-start/stop action (P6.2 acceptance: "the action toggles tracking"). Proxies
 * to [WatchFacade.startTracking]/[WatchFacade.stopTracking] — the same start/stop seam the Wear OS
 * UI already binds to — so a widget-initiated trip behaves identically to a watch-initiated one.
 */
class ToggleTrackingAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val facade = KoinPlatform.getKoin().getOrNull<WatchFacade>() ?: return
        val wasTracking = parameters[IsTrackingKey] ?: false
        if (wasTracking) facade.stopTracking() else facade.startTracking()
        MileageSummaryWidget().update(context, glanceId)
    }
}

/** Registers [MileageSummaryWidget] with the platform (declared in the manifest). */
class MileageSummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MileageSummaryWidget()
}
