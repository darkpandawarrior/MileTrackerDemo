package com.mileway.qs

import android.service.quicksettings.Tile
import com.mileway.core.data.watch.WatchSyncPayload

/**
 * P7.3: pure mapper from the cached [WatchSyncPayload] (same cache-only source
 * `MileageSummaryWidget` reads — see `SnapshotCache`'s doc comment for why a tile must never touch
 * Room directly) to the [Tile] state/label the Quick Settings tile renders. Kept dependency-free
 * (no `Tile` construction, only the fields the service needs to set) so it is unit-testable without
 * a `TileService` host.
 */
data class TrackingTileUiState(
    val tileState: Int,
    val label: String,
    val subtitle: String?,
)

fun WatchSyncPayload?.toTrackingTileUiState(): TrackingTileUiState =
    when {
        this == null -> TrackingTileUiState(Tile.STATE_INACTIVE, "Start tracking", null)
        isTracking && isPaused -> TrackingTileUiState(Tile.STATE_ACTIVE, "Tracking paused", "Tap to stop")
        isTracking -> TrackingTileUiState(Tile.STATE_ACTIVE, "Tracking", "Tap to stop")
        else -> TrackingTileUiState(Tile.STATE_INACTIVE, "Start tracking", null)
    }
