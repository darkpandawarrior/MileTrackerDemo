package com.mileway.wear

import com.mileway.core.data.model.display.SurfaceSnapshot
import com.mileway.feature.tracking.watch.TripSummary

/**
 * P2.4: pure, watchos-adjacent (JVM-testable) mapper from the shared [SurfaceSnapshot] to the
 * dashboard's rendering-ready [WearRootUiState]. Kept free of any `androidx.wear`/Compose import so
 * it can be unit-tested with a plain JUnit/kotlin.test runner (see `WearPresentationTest.kt`) —
 * mirrors the split `core:data`'s own `SurfaceSnapshotProducer` uses (pure fold, platform renderers
 * just lay the result out).
 *
 * P2.5 adds [toTripListItems], the equally pure mapper from [TripSummary] (the [WatchFacade]
 * value type) to [TripListItemUi] — trip-list/detail rendering-ready rows.
 */
object WearPresentation {
    fun toUiState(snapshot: SurfaceSnapshot): WearRootUiState =
        WearRootUiState(
            todayDistanceKm = snapshot.todayDistanceKm,
            weekDistanceKm = snapshot.weekDistanceKm,
            isTracking = snapshot.isTracking,
            weekGoalKm = snapshot.weekGoalKm,
            weekGoalProgress = snapshot.weekGoalProgress,
        )

    /** Maps [WatchFacade.recentTrips]' raw [TripSummary]s into display-ready [TripListItemUi] rows. */
    fun toTripListItems(trips: List<TripSummary>): List<TripListItemUi> = trips.map { it.toTripListItemUi() }

    private fun TripSummary.toTripListItemUi() =
        TripListItemUi(
            id = id,
            label = label.ifBlank { UNNAMED_TRIP_LABEL },
            km = km,
            endMs = endMs,
        )

    private const val UNNAMED_TRIP_LABEL = "Trip"
}

/**
 * Rendering-ready state for [WearRootScreen] — today/week distance, the tracking pill and the
 * week-goal progress ring, per P2.4's acceptance. Deliberately narrower than [SurfaceSnapshot]
 * (no trip counts/action badges — those aren't part of this task's dashboard).
 *
 * P2.5: [trips] backs the trip-list surface and [selectedTripId] drives which of [WearScreen]s is
 * shown — `null` means the dashboard, [WearScreen.TripList] the list, [WearScreen.TripDetail] the
 * detail surface for the trip matching [selectedTripId] in [trips].
 */
data class WearRootUiState(
    val todayDistanceKm: Double = 0.0,
    val weekDistanceKm: Double = 0.0,
    val isTracking: Boolean = false,
    val weekGoalKm: Double = SurfaceSnapshot.DEFAULT_WEEK_GOAL_KM,
    val weekGoalProgress: Float = 0f,
    val trips: List<TripListItemUi> = emptyList(),
    val screen: WearScreen = WearScreen.Dashboard,
    val selectedTripId: String? = null,
) {
    /** The [TripListItemUi] matching [selectedTripId], resolved once for [WearRootScreen] to render. */
    val selectedTrip: TripListItemUi?
        get() = trips.firstOrNull { it.id == selectedTripId }
}

/** P2.5: the single-activity screen `when` — [WearActivity][com.mileway.wear.WearActivity] never
 * navigates to a different Activity/Composable destination, it just swaps which of these renders
 * inside the one [WearRootScreen] (biciradar/P2.4 pattern, continued). */
enum class WearScreen {
    Dashboard,
    TripList,
    TripDetail,
}

/** A single rendering-ready row for the trip list/detail surfaces. */
data class TripListItemUi(
    val id: String,
    val label: String,
    val km: Double,
    val endMs: Long,
)
