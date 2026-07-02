package com.mileway.core.data.watch

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * The peer-sync seam between a phone and its paired watch, pure `commonMain` — no platform
 * transport types leak in here. Both the Android DataLayer implementation (PLAN_V23 P2.8/P2.9)
 * and the Apple `WCSession` implementation (P4.5, bridged from Swift) implement this same
 * contract, so [com.mileway.core.data.watch.WatchFacade] and any phone-side sync orchestration
 * can depend on one interface regardless of platform.
 *
 * [NoopWatchSyncBridge] is the default binding wherever no real transport exists yet — the FOSS
 * `noGms` flavor (GMS/DataLayer refs are confined to `wear/src/gms` and `app/src/gms` per the
 * flavor-isolation gotcha in PLAN_V23 §7) and tests both bind to it.
 */
interface WatchSyncBridge {
    /** Sends the latest [WatchSyncPayload] to the paired peer. */
    suspend fun push(payload: WatchSyncPayload)

    /** The most recent [WatchSyncPayload] received from the paired peer, if any. */
    suspend fun latest(): WatchSyncPayload?

    /** A live stream of [WatchSyncPayload]s as they arrive from the paired peer. */
    fun observeIncoming(): Flow<WatchSyncPayload>
}

/**
 * No-op [WatchSyncBridge]: never actually reaches a peer. [push] is a discard, [latest] always
 * returns `null`, and [observeIncoming] never emits. Used wherever no real transport is wired —
 * the FOSS `noGms` flavor, tests, and any platform without a sync implementation yet.
 */
class NoopWatchSyncBridge : WatchSyncBridge {
    private val incoming = MutableSharedFlow<WatchSyncPayload>()

    override suspend fun push(payload: WatchSyncPayload) {
        // Intentionally a no-op: no transport is wired behind this binding.
    }

    override suspend fun latest(): WatchSyncPayload? = null

    override fun observeIncoming(): Flow<WatchSyncPayload> = incoming.asSharedFlow()
}
