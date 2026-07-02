package com.mileway.qs

import android.service.quicksettings.Tile
import com.mileway.core.data.watch.WatchSyncPayload
import org.junit.Test
import kotlin.test.assertEquals

/** P7.3: unit test on the pure tile-state mapper (Acceptance clause). */
class TrackingTileStateTest {
    @Test
    fun `null payload maps to inactive start state`() {
        val ui = null.toTrackingTileUiState()
        assertEquals(Tile.STATE_INACTIVE, ui.tileState)
        assertEquals("Start tracking", ui.label)
        assertEquals(null, ui.subtitle)
    }

    @Test
    fun `not tracking maps to inactive start state`() {
        val ui = WatchSyncPayload(isTracking = false).toTrackingTileUiState()
        assertEquals(Tile.STATE_INACTIVE, ui.tileState)
        assertEquals("Start tracking", ui.label)
    }

    @Test
    fun `tracking maps to active stop state`() {
        val ui = WatchSyncPayload(isTracking = true, isPaused = false).toTrackingTileUiState()
        assertEquals(Tile.STATE_ACTIVE, ui.tileState)
        assertEquals("Tracking", ui.label)
        assertEquals("Tap to stop", ui.subtitle)
    }

    @Test
    fun `tracking and paused still maps to active stop state`() {
        val ui = WatchSyncPayload(isTracking = true, isPaused = true).toTrackingTileUiState()
        assertEquals(Tile.STATE_ACTIVE, ui.tileState)
        assertEquals("Tracking paused", ui.label)
        assertEquals("Tap to stop", ui.subtitle)
    }
}
