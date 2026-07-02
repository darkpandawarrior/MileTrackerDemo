package com.mileway.core.data.watch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * P2.10: [TrackingCommandCodec] is the platform-independent encode/decode pair the watch-side
 * `WearTrackingCommandSender` (`wear/src/gms`) and the phone-side `WearTrackingCommandService`
 * (`app/src/gms`) share for the `MessageClient` byte payload — tested here without any
 * `MessageClient`/`CapabilityClient` runtime, mirroring [SnapshotCacheCodecTest]'s pure-codec
 * style.
 */
class TrackingCommandCodecTest {
    @Test
    fun `encode then decode round trips a start command exactly`() {
        val command = TrackingCommand(action = TrackingCommand.Action.START, token = "trip-token-123")

        val encoded = TrackingCommandCodec.encode(command)
        val decoded = TrackingCommandCodec.decode(encoded)

        assertEquals(command, decoded)
    }

    @Test
    fun `encode then decode round trips a stop command exactly`() {
        val command = TrackingCommand(action = TrackingCommand.Action.STOP, token = "trip-token-456")

        val encoded = TrackingCommandCodec.encode(command)
        val decoded = TrackingCommandCodec.decode(encoded)

        assertEquals(command, decoded)
    }

    @Test
    fun `decode of null bytes returns null`() {
        assertNull(TrackingCommandCodec.decode(null))
    }

    @Test
    fun `decode of empty bytes returns null`() {
        assertNull(TrackingCommandCodec.decode(ByteArray(0)))
    }

    @Test
    fun `decode of malformed bytes returns null instead of throwing`() {
        assertNull(TrackingCommandCodec.decode("{not-valid-json".encodeToByteArray()))
    }

    @Test
    fun `decode tolerates unknown fields from a newer watch build`() {
        val command = TrackingCommand(action = TrackingCommand.Action.START, token = "trip-token-789")
        val encoded = TrackingCommandCodec.encode(command).decodeToString()
        val withExtraField = (encoded.dropLast(1) + ""","futureField":"ignored"}""").encodeToByteArray()

        assertEquals(command, TrackingCommandCodec.decode(withExtraField))
    }
}
