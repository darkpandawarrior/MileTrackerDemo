package com.mileway.wear

import com.mileway.core.data.model.display.SurfaceSnapshot

/**
 * P2.4: pure, watchos-adjacent (JVM-testable) mapper from the shared [SurfaceSnapshot] to the
 * dashboard's rendering-ready [WearRootUiState]. Kept free of any `androidx.wear`/Compose import so
 * it can be unit-tested with a plain JUnit/kotlin.test runner (see `WearPresentationTest.kt`) —
 * mirrors the split `core:data`'s own `SurfaceSnapshotProducer` uses (pure fold, platform renderers
 * just lay the result out).
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
}

/**
 * Rendering-ready state for [WearRootScreen] — today/week distance, the tracking pill and the
 * week-goal progress ring, per P2.4's acceptance. Deliberately narrower than [SurfaceSnapshot]
 * (no trip counts/action badges — those aren't part of this task's dashboard).
 */
data class WearRootUiState(
    val todayDistanceKm: Double = 0.0,
    val weekDistanceKm: Double = 0.0,
    val isTracking: Boolean = false,
    val weekGoalKm: Double = SurfaceSnapshot.DEFAULT_WEEK_GOAL_KM,
    val weekGoalProgress: Float = 0f,
)
