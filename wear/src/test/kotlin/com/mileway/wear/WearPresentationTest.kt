package com.mileway.wear

import com.mileway.core.data.model.display.SurfaceSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * P2.4: JVM-unit coverage for the pure [WearPresentation] mapper (`:wear:testNoGmsDebugUnitTest`
 * per the plan's acceptance) — no Compose/Robolectric needed since it's plain data mapping.
 */
class WearPresentationTest {

    @Test
    fun `maps today and week distance straight through`() {
        val snapshot = SurfaceSnapshot(todayDistanceKm = 12.4, weekDistanceKm = 58.7)

        val uiState = WearPresentation.toUiState(snapshot)

        assertEquals(12.4, uiState.todayDistanceKm, 0.0)
        assertEquals(58.7, uiState.weekDistanceKm, 0.0)
    }

    @Test
    fun `maps isTracking through unchanged`() {
        assertEquals(true, WearPresentation.toUiState(SurfaceSnapshot(isTracking = true)).isTracking)
        assertEquals(false, WearPresentation.toUiState(SurfaceSnapshot(isTracking = false)).isTracking)
    }

    @Test
    fun `maps week goal km and derives progress from the snapshot`() {
        val snapshot = SurfaceSnapshot(weekDistanceKm = 50.0, weekGoalKm = 100.0)

        val uiState = WearPresentation.toUiState(snapshot)

        assertEquals(100.0, uiState.weekGoalKm, 0.0)
        assertEquals(0.5f, uiState.weekGoalProgress, 0.0001f)
    }

    @Test
    fun `week goal progress is clamped to 1f when distance exceeds the goal`() {
        val snapshot = SurfaceSnapshot(weekDistanceKm = 150.0, weekGoalKm = 100.0)

        val uiState = WearPresentation.toUiState(snapshot)

        assertEquals(1f, uiState.weekGoalProgress, 0.0001f)
    }

    @Test
    fun `default snapshot maps to a zeroed idle ui state`() {
        val uiState = WearPresentation.toUiState(SurfaceSnapshot())

        assertEquals(WearRootUiState(), uiState)
    }
}
