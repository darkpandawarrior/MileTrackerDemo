package com.mileway.core.data.watch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * P6.1: [SnapshotCacheCodec] is the platform-independent encode/decode pair both
 * [SnapshotCache] actuals (Android DataStore, iOS `NSUserDefaults` App Group) share — tested
 * here without any platform storage so the wire-shape contract is verified on every target,
 * mirroring [WatchSyncPayloadTest]'s pure-codec style.
 */
class SnapshotCacheCodecTest {
    private val payload =
        WatchSyncPayload(
            todayKm = 12.5,
            weekKm = 48.0,
            tripCount = 6,
            isTracking = true,
            isPaused = false,
            weekGoalProgress = 0.6f,
            lastTripLabel = "Morning Commute",
            updatedAtMs = 1_700_000_000_000L,
        )

    @Test
    fun `encode then decode round trips the payload exactly`() {
        val encoded = SnapshotCacheCodec.encode(payload)
        val decoded = SnapshotCacheCodec.decode(encoded)

        assertEquals(payload, decoded)
    }

    @Test
    fun `decode of null returns null`() {
        assertNull(SnapshotCacheCodec.decode(null))
    }

    @Test
    fun `decode of empty string returns null`() {
        assertNull(SnapshotCacheCodec.decode(""))
    }

    @Test
    fun `decode of malformed json returns null instead of throwing`() {
        assertNull(SnapshotCacheCodec.decode("{not-valid-json"))
    }

    @Test
    fun `decode tolerates unknown fields from a newer app build`() {
        val encoded = SnapshotCacheCodec.encode(payload)
        val withExtraField = encoded.dropLast(1) + ""","futureField":"ignored"}"""

        assertEquals(payload, SnapshotCacheCodec.decode(withExtraField))
    }

    @Test
    fun `default payload round trips`() {
        val default = WatchSyncPayload()
        assertEquals(default, SnapshotCacheCodec.decode(SnapshotCacheCodec.encode(default)))
    }
}
