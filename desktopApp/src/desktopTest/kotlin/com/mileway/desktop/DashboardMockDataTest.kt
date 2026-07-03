package com.mileway.desktop

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** D.2 smoke test: the desktop dashboard's mock data folds into a sane [SurfaceSnapshot] + trip list. */
class DashboardMockDataTest {
    private val now = 10 * 86_400_000L // fixed instant, deterministic

    @Test
    fun `mockSnapshot reports today and week distance from the mock trips`() {
        val snapshot = mockSnapshot(now)

        assertTrue(snapshot.todayTrips >= 1)
        assertEquals(3, snapshot.weekTrips)
        assertTrue(snapshot.weekDistanceKm > 0.0)
    }

    @Test
    fun `mockTripRows returns all mock trips newest-first`() {
        val rows = mockTripRows(now)

        assertEquals(listOf("d1", "d2", "d3"), rows.map { it.token })
    }
}
