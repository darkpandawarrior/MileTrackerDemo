package com.mileway.core.data.watch

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** P1.3: [NoopWatchSyncBridge] is the default [WatchSyncBridge] binding — never reaches a peer. */
class WatchSyncBridgeTest {
    private val payload = WatchSyncPayload(todayKm = 12.5, weekKm = 48.0, tripCount = 6)

    @Test
    fun `push is a discard and never surfaces on latest`() =
        runTest {
            val bridge: WatchSyncBridge = NoopWatchSyncBridge()

            bridge.push(payload)

            assertNull(bridge.latest())
        }

    @Test
    fun `latest is always null with no transport wired`() =
        runTest {
            val bridge: WatchSyncBridge = NoopWatchSyncBridge()

            assertNull(bridge.latest())
        }

    @Test
    fun `observeIncoming never emits on its own`() =
        runTest {
            val bridge: WatchSyncBridge = NoopWatchSyncBridge()
            var received: WatchSyncPayload? = null
            val collector =
                launch {
                    received = bridge.observeIncoming().first()
                }

            // Nothing pushes into a shared incoming flow that has no producer wired.
            collector.cancel()

            assertEquals(null, received)
        }
}
