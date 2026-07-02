package com.mileway.core.data.watch

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * P2.10: the watch->phone half of the peer-sync story (P2.9 is phone->watch). A watch-initiated
 * start/stop command, carrying the session token the phone's `WearTrackingCommandService`
 * (`app/src/gms`) needs to call `TrackingController.start`/`stop` with — the same token shape
 * `feature:tracking`'s `TrackingController` already uses (see
 * `feature.tracking.manager.TrackingController`; `core:data` cannot depend on `feature:tracking`,
 * so this is a plain token string, not a reference to that interface).
 *
 * Deliberately its own tiny wire type (not a reuse of [WatchSyncPayload], which flows the other
 * direction and carries snapshot data, not a command) so the two directions stay independently
 * versionable.
 */
@Serializable
data class TrackingCommand(
    val action: Action,
    val token: String,
) {
    @Serializable
    enum class Action { START, STOP }
}

/**
 * The JSON codec [TrackingCommand] transports travel over (Wear OS `MessageClient` bytes today;
 * any future watchOS `WCSession` message would reuse the same shape). Factored out exactly like
 * [SnapshotCacheCodec] so it is unit-testable without a platform-backed `MessageClient`.
 */
object TrackingCommandCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(command: TrackingCommand): ByteArray {
        val encoded = json.encodeToString(TrackingCommand.serializer(), command)
        return encoded.encodeToByteArray()
    }

    /** Returns null (rather than throwing) on any malformed/legacy-shape message payload. */
    fun decode(bytes: ByteArray?): TrackingCommand? {
        if (bytes == null || bytes.isEmpty()) return null
        return try {
            json.decodeFromString(TrackingCommand.serializer(), bytes.decodeToString())
        } catch (_: Exception) {
            null
        }
    }
}
