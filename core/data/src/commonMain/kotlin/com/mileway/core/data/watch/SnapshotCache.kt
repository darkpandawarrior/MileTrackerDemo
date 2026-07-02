package com.mileway.core.data.watch

import kotlinx.serialization.json.Json

/**
 * P6.1: a tiny persisted cache of the latest [WatchSyncPayload], readable by BOTH the main app
 * process and a short-lived widget/extension process without touching Room — widgets (Android
 * Glance `GlanceAppWidget.provideGlance`, iOS `WidgetKit` `TimelineProvider`) run in their own
 * process (or are re-launched cold on every timeline refresh) and cannot assume the app's
 * in-memory [com.mileway.core.data.model.display.SnapshotPublisher] is warm, and opening the full
 * `MilewayDatabase` from a widget update is the exact anti-pattern `MileageSummaryWidget`'s own
 * doc comment (see `widget/.../MileageSummaryWidget.kt`) flags as the thing L.1/P6.1 should
 * replace.
 *
 * Deliberately the SAME wire shape as [WatchSyncPayload] (the watch peer-sync contract), not the
 * richer [com.mileway.core.data.model.display.SurfaceSnapshot] — a widget renders the same tiny
 * subset a watch complication does, so one payload type and one JSON codec serve both consumers.
 *
 * Platform actuals: Android = a small DataStore file in the app's own storage (readable by
 * `MileageSummaryWidget`'s `GlanceAppWidgetReceiver` because a home-screen widget runs in-process
 * on Android, not a separate app); iOS = an App Group shared container
 * (`NSUserDefaults(suiteName:)`) so a `WidgetKit` extension process — which does NOT share the
 * host app's sandbox — can read it (P6.3 wires the extension target + entitlement; this task only
 * needs the write/read contract + codec to exist and be correct ahead of that).
 */
interface SnapshotCache {
    /** Persists the latest [WatchSyncPayload], overwriting whatever was cached before. */
    suspend fun write(payload: WatchSyncPayload)

    /** Reads the most recently cached [WatchSyncPayload], or null if nothing has been written yet. */
    suspend fun read(): WatchSyncPayload?
}

/**
 * The JSON codec [SnapshotCache] actuals share, factored out so it is unit-testable without a
 * platform-backed DataStore/UserDefaults (see [SnapshotCacheCodecTest]). Reuses the same
 * [kotlinx.serialization.json.Json] instance shape the watch wire contract already relies on
 * (`ignoreUnknownKeys = true`, matching `CoreDataModule`'s `Json` singleton) so a newer app build
 * never crash-loops a widget process still holding an older cached payload shape.
 */
object SnapshotCacheCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(payload: WatchSyncPayload): String = json.encodeToString(WatchSyncPayload.serializer(), payload)

    /** Returns null (rather than throwing) on any malformed/legacy-shape cached value. */
    fun decode(raw: String?): WatchSyncPayload? {
        if (raw.isNullOrEmpty()) return null
        return try {
            json.decodeFromString(WatchSyncPayload.serializer(), raw)
        } catch (_: Exception) {
            null
        }
    }
}
