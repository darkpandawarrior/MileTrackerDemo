package com.mileway.qs

import android.service.quicksettings.TileService
import com.mileway.core.data.watch.SnapshotCache
import com.mileway.feature.tracking.watch.WatchFacade
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

private const val TAG = "TrackingTileService"

/**
 * P7.3: a Quick Settings tile that starts/stops tracking without opening the app UI, mirroring
 * `MileageSummaryWidget`'s (P6.2) Glance quick-start action — same cache-only read
 * ([SnapshotCache], never Room) for the tile's resting state, same [WatchFacade] start/stop proxy
 * for the toggle, resolved via `KoinPlatform.getKoin()` since a `TileService` is a
 * framework-instantiated component Koin cannot constructor-inject (same pattern the widget/wear
 * tile use).
 */
class TrackingTileService : TileService() {
    private var scope: CoroutineScope? = null

    override fun onStartListening() {
        super.onStartListening()
        val job = SupervisorJob()
        scope = CoroutineScope(job + Dispatchers.Main.immediate).also { it.launch { refresh() } }
    }

    override fun onStopListening() {
        super.onStopListening()
        scope?.cancel()
        scope = null
    }

    override fun onClick() {
        super.onClick()
        val facade = KoinPlatform.getKoin().getOrNull<WatchFacade>() ?: return
        val cache = KoinPlatform.getKoin().getOrNull<SnapshotCache>()
        (scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)).launch {
            val wasTracking = cache?.read()?.isTracking == true
            if (wasTracking) facade.stopTracking() else facade.startTracking()
            refresh()
        }
    }

    private suspend fun refresh() {
        val cache = KoinPlatform.getKoin().getOrNull<SnapshotCache>()
        val payload =
            runCatching { cache?.read() }
                .onFailure { Napier.w(tag = TAG, message = "tile refresh failed", throwable = it) }
                .getOrNull()
        val ui = payload.toTrackingTileUiState()
        qsTile?.apply {
            state = ui.tileState
            label = ui.label
            subtitle = ui.subtitle
            updateTile()
        }
    }
}
