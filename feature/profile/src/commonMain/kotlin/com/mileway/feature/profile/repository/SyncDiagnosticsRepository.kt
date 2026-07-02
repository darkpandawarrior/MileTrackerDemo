package com.mileway.feature.profile.repository

import com.mileway.feature.profile.model.SyncMetrics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random
import kotlin.time.Clock

/**
 * PLAN_V22 P6.7: in-memory counter backing Settings' `SyncDiagnosticsCard` — plausible, locally
 * generated metrics (no network call; Mileway has no backend to sync against yet, see CLAUDE.md
 * "The backend"). [forceSync] simulates a local upload job: a short delay, then a randomized bump
 * to the synced counters (occasionally a failed attempt), mirroring the shape of a real sync
 * result without pretending to talk to a server.
 */
class SyncDiagnosticsRepository(private val clock: Clock = Clock.System) {
    private val _metrics =
        MutableStateFlow(
            SyncMetrics(
                locationsSynced = 128,
                eventsSynced = 42,
                failedAttempts = 1,
                lastSyncTimeMs = null,
            ),
        )
    val metrics: StateFlow<SyncMetrics> = _metrics.asStateFlow()

    /** Runs a local upload-simulation job: marks syncing, waits briefly, then updates counters. */
    suspend fun forceSync() {
        _metrics.update { it.copy(isSyncing = true) }
        delay(SYNC_SIMULATION_DELAY_MS)
        val newLocations = Random.nextInt(MIN_LOCATIONS_PER_SYNC, MAX_LOCATIONS_PER_SYNC + 1)
        val newEvents = Random.nextInt(MIN_EVENTS_PER_SYNC, MAX_EVENTS_PER_SYNC + 1)
        val failed = if (Random.nextInt(FAILURE_ODDS) == 0) 1 else 0
        _metrics.update {
            it.copy(
                locationsSynced = it.locationsSynced + newLocations,
                eventsSynced = it.eventsSynced + newEvents,
                failedAttempts = it.failedAttempts + failed,
                lastSyncTimeMs = clock.now().toEpochMilliseconds(),
                isSyncing = false,
            )
        }
    }

    private companion object {
        const val SYNC_SIMULATION_DELAY_MS = 900L
        const val MIN_LOCATIONS_PER_SYNC = 3
        const val MAX_LOCATIONS_PER_SYNC = 12
        const val MIN_EVENTS_PER_SYNC = 1
        const val MAX_EVENTS_PER_SYNC = 5
        const val FAILURE_ODDS = 6
    }
}
