package com.mileway.sharedwatch

import com.mileway.core.data.database.buildMilewayDatabase
import com.mileway.core.data.model.display.InMemorySnapshotPublisher

/**
 * P3.3: the Swift-visible entrypoint into the `MilewayWatch.framework` — the single object the
 * future watch app's Swift `actor` (P4.3's `MilewayWatchGraph`) constructs to obtain a
 * [WatchDomainFacade].
 *
 * Lives in `watchosMain` (not `commonMain`) because it calls `core:data`'s `buildMilewayDatabase()`,
 * which is only defined on the `appleMain` intermediate source set (shared by `iosMain` +
 * `watchosMain` via `applyDefaultHierarchyTemplate()` — see P3.2's PROGRESS entry). `watchosMain`
 * sits under `appleMain` in this module's own default hierarchy, so it resolves that function.
 *
 * A fresh [InMemorySnapshotPublisher] here starts empty (`SurfaceSnapshot()` defaults) until the
 * phone pushes a [com.mileway.core.data.watch.WatchSyncPayload] over `WatchSyncBridge` (P4.5) and
 * the watch-side sync wiring publishes it — that wiring is a later Phase 4 task; this factory only
 * establishes the seam a Swift caller can already build against today.
 */
object WatchFacadeFactory {
    /** Builds a fresh, watch-process-local [WatchDomainFacade] over the on-device Room database. */
    fun create(): WatchDomainFacade {
        val database = buildMilewayDatabase()
        val snapshotPublisher = InMemorySnapshotPublisher()
        return WatchDomainFacade(
            snapshotPublisher = snapshotPublisher,
            savedTrackDao = database.savedTrackDao(),
        )
    }
}
