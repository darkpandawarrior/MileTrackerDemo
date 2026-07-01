package com.mileway.feature.tracking.checkin

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [RoundTripClassifier].
 *
 * Mirrors the [CheckInValidator] test pattern: fixed mock coordinates, boundary cases, and
 * degenerate-input cases.
 */
class RoundTripClassifierTest {
    private val officeLat = 18.5204
    private val officeLng = 73.8567

    // ── Near start/end + large tracked distance → true ─────────────────────────

    @Test
    fun `same start and end coordinates with large tracked distance is a round trip`() {
        val result =
            RoundTripClassifier.isRoundTrip(
                startLat = officeLat,
                startLng = officeLng,
                endLat = officeLat,
                endLng = officeLng,
                totalDistanceKm = 25.0,
            )
        assertTrue(result, "Identical start/end with real tracked distance should be a round trip")
    }

    @Test
    fun `start and end within proximity threshold with large tracked distance is a round trip`() {
        // ~1 km north of start, well within the default 2 km threshold.
        val offsetLat = 1.0 / 111.0
        val result =
            RoundTripClassifier.isRoundTrip(
                startLat = officeLat,
                startLng = officeLng,
                endLat = officeLat + offsetLat,
                endLng = officeLng,
                totalDistanceKm = 40.0,
            )
        assertTrue(result, "End within 2 km of start after a long tracked trip should be a round trip")
    }

    // ── Far-apart start/end → false ─────────────────────────────────────────────

    @Test
    fun `far apart start and end is not a round trip`() {
        // ~10 km north of start, well outside the default 2 km threshold.
        val offsetLat = 10.0 / 111.0
        val result =
            RoundTripClassifier.isRoundTrip(
                startLat = officeLat,
                startLng = officeLng,
                endLat = officeLat + offsetLat,
                endLng = officeLng,
                totalDistanceKm = 40.0,
            )
        assertFalse(result, "End 10 km from start should not be classified as a round trip")
    }

    @Test
    fun `start and end just outside custom proximity threshold is not a round trip`() {
        // ~3 km away, threshold set to 2 km.
        val offsetLat = 3.0 / 111.0
        val result =
            RoundTripClassifier.isRoundTrip(
                startLat = officeLat,
                startLng = officeLng,
                endLat = officeLat + offsetLat,
                endLng = officeLng,
                totalDistanceKm = 20.0,
                proximityThresholdKm = 2.0,
            )
        assertFalse(result)
    }

    // ── Degenerate zero-distance → false ────────────────────────────────────────

    @Test
    fun `zero tracked distance at same coordinates is not a round trip`() {
        val result =
            RoundTripClassifier.isRoundTrip(
                startLat = officeLat,
                startLng = officeLng,
                endLat = officeLat,
                endLng = officeLng,
                totalDistanceKm = 0.0,
            )
        assertFalse(result, "Never having moved should not be classified as a round trip")
    }

    @Test
    fun `negligible tracked distance below the minimum threshold is not a round trip`() {
        val result =
            RoundTripClassifier.isRoundTrip(
                startLat = officeLat,
                startLng = officeLng,
                endLat = officeLat,
                endLng = officeLng,
                totalDistanceKm = 0.05,
            )
        assertFalse(result)
    }

    // ── Custom proximity threshold widening the match ───────────────────────────

    @Test
    fun `wider custom proximity threshold accepts a farther start-end gap`() {
        // ~4 km away, but threshold widened to 5 km.
        val offsetLat = 4.0 / 111.0
        val result =
            RoundTripClassifier.isRoundTrip(
                startLat = officeLat,
                startLng = officeLng,
                endLat = officeLat + offsetLat,
                endLng = officeLng,
                totalDistanceKm = 30.0,
                proximityThresholdKm = 5.0,
            )
        assertTrue(result)
    }
}
