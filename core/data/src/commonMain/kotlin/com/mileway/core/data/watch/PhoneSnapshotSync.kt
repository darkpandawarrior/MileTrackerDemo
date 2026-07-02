package com.mileway.core.data.watch

import com.mileway.core.data.model.display.SnapshotPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * P2.9: the phone-side half of the phone->watch snapshot sync. Observes the app's existing
 * [SnapshotPublisher] (already the single source of truth Glance/WidgetKit surfaces render from —
 * see [com.mileway.core.data.model.display.SnapshotPublisher]) and pushes every new
 * [com.mileway.core.data.model.display.SurfaceSnapshot] through [WatchSyncBridge] as soon as it
 * changes (a trip start/stop/completion, or any other producer call), so the watch tile/
 * complication/first paint has fresh data offline without the phone needing to be reachable at
 * watch-render time.
 *
 * Deliberately platform-agnostic (pure `commonMain`, over the [WatchSyncBridge] seam from P1.3)
 * rather than gms-only: on the `noGms` flavor [WatchSyncBridge] resolves to [NoopWatchSyncBridge]
 * (push is a discard), so starting this on every flavor is free/harmless and keeps the "observe +
 * push" wiring out of flavor-specific source sets — only the transport underneath varies per
 * flavor (`wear/src/gms`'s `WearDataLayerSyncBridge`, `app/src/gms`'s
 * `WearDataLayerWatchSyncBridge`), exactly the split PLAN_V23 §7's flavor-isolation gotcha calls
 * for.
 */
class PhoneSnapshotSync(
    private val snapshotPublisher: SnapshotPublisher,
    private val watchSyncBridge: WatchSyncBridge,
) {
    /** Starts observing [SnapshotPublisher.snapshot] and pushing every change. Call once at app start. */
    fun start(scope: CoroutineScope): Job =
        snapshotPublisher.snapshot
            .onEach { snapshot -> watchSyncBridge.push(snapshot.toWatchPayload()) }
            .launchIn(scope)
}
