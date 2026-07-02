package com.mileway.core.data.watch

import com.mileway.core.data.model.display.SurfaceSnapshot
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/** P1.1: [WatchSyncPayload] is the phone<->watch wire contract derived from [SurfaceSnapshot]. */
class WatchSyncPayloadTest {
    private val snapshot =
        SurfaceSnapshot(
            todayDistanceKm = 12.5,
            todayTrips = 2,
            weekDistanceKm = 48.0,
            weekTrips = 6,
            isTracking = true,
            lastUpdatedEpochMs = 1_700_000_000_000L,
            isPaused = false,
            qualityScore = 87,
            weekGoalKm = 80.0,
            actionRequiredCount = 1,
            lastTripLabel = "Morning Commute",
        )

    @Test
    fun `snapshot to payload projects only the watch-rendered fields`() {
        val payload = snapshot.toWatchPayload()

        assertEquals(snapshot.todayDistanceKm, payload.todayKm)
        assertEquals(snapshot.weekDistanceKm, payload.weekKm)
        assertEquals(snapshot.weekTrips, payload.tripCount)
        assertEquals(snapshot.isTracking, payload.isTracking)
        assertEquals(snapshot.isPaused, payload.isPaused)
        assertEquals(snapshot.weekGoalProgress, payload.weekGoalProgress)
        assertEquals(snapshot.lastTripLabel, payload.lastTripLabel)
        assertEquals(snapshot.lastUpdatedEpochMs, payload.updatedAtMs)
    }

    @Test
    fun `round trip through payload preserves every watch-rendered field`() {
        val payload = snapshot.toWatchPayload()
        val reconstructed = payload.toSurfaceSnapshot()

        assertEquals(snapshot.todayDistanceKm, reconstructed.todayDistanceKm)
        assertEquals(snapshot.weekDistanceKm, reconstructed.weekDistanceKm)
        assertEquals(snapshot.weekTrips, reconstructed.weekTrips)
        assertEquals(snapshot.isTracking, reconstructed.isTracking)
        assertEquals(snapshot.isPaused, reconstructed.isPaused)
        assertEquals(snapshot.weekGoalProgress, reconstructed.weekGoalProgress)
        assertEquals(snapshot.lastTripLabel, reconstructed.lastTripLabel)
        assertEquals(snapshot.lastUpdatedEpochMs, reconstructed.lastUpdatedEpochMs)
    }

    @Test
    fun `json encode decode round trip is stable`() {
        val payload = snapshot.toWatchPayload()

        val json = Json.encodeToString(WatchSyncPayload.serializer(), payload)
        val decoded = Json.decodeFromString(WatchSyncPayload.serializer(), json)

        assertEquals(payload, decoded)
    }

    @Test
    fun `json field names are stable for the wire contract`() {
        val payload = snapshot.toWatchPayload()
        val json = Json.encodeToString(WatchSyncPayload.serializer(), payload)

        assertEquals(true, json.contains("\"todayKm\""))
        assertEquals(true, json.contains("\"weekKm\""))
        assertEquals(true, json.contains("\"tripCount\""))
        assertEquals(true, json.contains("\"isTracking\""))
        assertEquals(true, json.contains("\"isPaused\""))
        assertEquals(true, json.contains("\"weekGoalProgress\""))
        assertEquals(true, json.contains("\"lastTripLabel\""))
        assertEquals(true, json.contains("\"updatedAtMs\""))
    }

    @Test
    fun `default payload round trips through a zero-progress snapshot`() {
        val payload = WatchSyncPayload()
        val reconstructed = payload.toSurfaceSnapshot()
        val reprojected = reconstructed.toWatchPayload()

        assertEquals(payload, reprojected)
    }
}
