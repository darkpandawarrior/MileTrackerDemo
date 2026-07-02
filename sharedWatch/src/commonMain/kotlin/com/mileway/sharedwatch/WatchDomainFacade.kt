package com.mileway.sharedwatch

import com.mileway.core.data.dao.SavedTrackDao
import com.mileway.core.data.model.display.SnapshotPublisher
import com.mileway.core.data.model.display.SurfaceSnapshot
import com.mileway.core.data.model.display.toDisplayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * P3.3: the single read-only facade the headless `MilewayWatch.framework` exports to Swift — the
 * watchos-safe sibling of `feature:tracking`'s `WatchFacade` (P1.2).
 *
 * **Why a sibling instead of reusing `WatchFacade` directly** (documented deviation from the
 * plan's literal "export WatchFacade" line, same reasoning P1.2 already flagged): `WatchFacade`
 * lives in `feature:tracking`, which applies `mileway.cmp.feature` (Compose Multiplatform) and
 * therefore has no `watchos*` target — PLAN_V23 §6 is explicit that Compose must never target
 * watchOS. `:sharedWatch` can only `export(project(":core:data"))` (see this module's
 * build.gradle.kts), so [WatchDomainFacade] is built directly on `core:data`'s already
 * watchos-safe surface: [SnapshotPublisher] (unchanged — the exact channel `WatchFacade` also
 * delegates to) and [SavedTrackDao] (read-only here; `SavedTrackRepository`'s write paths and
 * `TrackingController` start/stop are `feature:tracking` types and out of scope for a headless
 * read surface — PLAN_V23 §6 explicitly defers "standalone on-watch GPS" and any watch-initiated
 * command surface to a later, opportunistic task).
 *
 * Exposes only plain value types (no Room entities) across its public API, same as `WatchFacade`.
 */
class WatchDomainFacade(
    private val snapshotPublisher: SnapshotPublisher,
    private val savedTrackDao: SavedTrackDao,
) {
    /** Live [SurfaceSnapshot] stream — the same one the phone's Glance/WidgetKit surfaces render. */
    fun observeSnapshot(): Flow<SurfaceSnapshot> = snapshotPublisher.snapshot

    /** The most recent completed trips, newest first, capped at [limit]. */
    fun recentTrips(limit: Int): Flow<List<WatchTripSummary>> =
        savedTrackDao.getCompletedTracks().map { tracks ->
            tracks
                .map { it.toDisplayData() }
                .sortedByDescending { it.endTime }
                .take(limit)
                .map { WatchTripSummary(id = it.token, label = it.name.orEmpty(), km = it.distanceKm, endMs = it.endTime) }
        }
}
