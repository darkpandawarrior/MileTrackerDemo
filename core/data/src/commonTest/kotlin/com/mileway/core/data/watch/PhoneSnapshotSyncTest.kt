package com.mileway.core.data.watch

import com.mileway.core.data.model.display.InMemorySnapshotPublisher
import com.mileway.core.data.model.display.SurfaceSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * P2.9: [PhoneSnapshotSync] observes [InMemorySnapshotPublisher] and pushes every emission
 * through [WatchSyncBridge], using [SnapshotCacheCodec]'s already-tested wire shape
 * ([toWatchPayload]/[WatchSyncPayloadTest]). A [FakeWatchSyncBridge] stands in for the real
 * `wear/src/gms`/`app/src/gms` DataLayer transport, which needs an Android/Play-Services runtime
 * this JVM unit test doesn't have.
 */
class PhoneSnapshotSyncTest {
    private class FakeWatchSyncBridge : WatchSyncBridge {
        val pushed = mutableListOf<WatchSyncPayload>()
        private val incoming = MutableSharedFlow<WatchSyncPayload>()

        override suspend fun push(payload: WatchSyncPayload) {
            pushed += payload
        }

        override suspend fun latest(): WatchSyncPayload? = pushed.lastOrNull()

        override fun observeIncoming(): Flow<WatchSyncPayload> = incoming.asSharedFlow()
    }

    @Test
    fun `start pushes the current snapshot immediately as a watch payload`() =
        runTest(UnconfinedTestDispatcher()) {
            val publisher = InMemorySnapshotPublisher()
            val bridge = FakeWatchSyncBridge()
            val sync = PhoneSnapshotSync(publisher, bridge)

            val job = sync.start(this)
            advanceUntilIdle()
            job.cancel()

            assertEquals(listOf(SurfaceSnapshot().toWatchPayload()), bridge.pushed)
        }

    @Test
    fun `every new snapshot publish triggers a new push`() =
        runTest(UnconfinedTestDispatcher()) {
            val publisher = InMemorySnapshotPublisher()
            val bridge = FakeWatchSyncBridge()
            val sync = PhoneSnapshotSync(publisher, bridge)

            val job = sync.start(this)
            advanceUntilIdle()

            val updated = SurfaceSnapshot(todayDistanceKm = 12.5, weekDistanceKm = 48.0, isTracking = true)
            publisher.publish(updated)
            advanceUntilIdle()
            job.cancel()

            assertEquals(
                listOf(SurfaceSnapshot().toWatchPayload(), updated.toWatchPayload()),
                bridge.pushed,
            )
        }
}
