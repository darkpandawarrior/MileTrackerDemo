package com.mileway.feature.profile.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

private class FixedClock(private val epochMs: Long) : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(epochMs)
}

/**
 * PLAN_V22 P6.7: [SyncDiagnosticsRepository] backs Settings' `SyncDiagnosticsCard` — an in-memory
 * counter (no network call; Mileway has no backend to sync against yet) that [forceSync] bumps to
 * simulate a real local upload job.
 */
class SyncDiagnosticsRepositoryTest {
    @Test
    fun `initial metrics have never synced and are not mid-sync`() {
        val repository = SyncDiagnosticsRepository()

        val initial = repository.metrics.value

        assertEquals(null, initial.lastSyncTimeMs)
        assertTrue(!initial.isSyncing)
    }

    @Test
    fun `forceSync bumps the synced counters and records a real last-sync time`() =
        runTest {
            val fixedNowMs = 1_700_000_000_000L
            val repository = SyncDiagnosticsRepository(clock = FixedClock(fixedNowMs))
            val before = repository.metrics.value

            repository.forceSync()

            val after = repository.metrics.value
            assertTrue(after.locationsSynced > before.locationsSynced)
            assertTrue(after.eventsSynced > before.eventsSynced)
            assertEquals(fixedNowMs, after.lastSyncTimeMs)
            assertTrue(!after.isSyncing)
        }
}
