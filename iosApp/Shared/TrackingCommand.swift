// Z.5d: the watchOS analogue of Android's `TrackingCommand`/`TrackingCommandCodec` (see
// `core/data/src/commonMain/kotlin/com/mileway/core/data/watch/TrackingCommand.kt`) — a
// watch-initiated start/stop message, mirroring `MilewaySyncModels.swift`'s pattern of a plain
// `Codable` struct shared between the `iosApp` and `MilewayWatch` targets (see `iosApp/project.yml`)
// so neither side needs the other's KMP framework to agree on wire shape.
//
// Deliberately its own tiny type (not reusing `MilewaySyncPayload`, which flows phone->watch and
// carries snapshot data, not a command) — same reasoning `TrackingCommand.kt` gives for not reusing
// `WatchSyncPayload` on the Android side.
import Foundation

struct TrackingCommand: Codable, Equatable {
    enum Action: String, Codable {
        case start
        case stop
    }

    var action: Action

    static let messageKey = "action"
}

/// `WCSession.sendMessage`/`didReceiveMessage` traffic in plain `[String: Any]` dictionaries, same
/// as `MilewaySyncCodec` for `updateApplicationContext` — this mirrors that codec's shape/fail-soft
/// contract for the message-based command channel.
enum TrackingCommandCodec {
    static func encode(_ command: TrackingCommand) -> [String: Any] {
        [TrackingCommand.messageKey: command.action.rawValue]
    }

    /// Returns `nil` (rather than throwing) on any malformed/unrecognized message, mirroring
    /// Android's `TrackingCommandCodec.decode` fail-soft contract.
    static func decode(_ message: [String: Any]) -> TrackingCommand? {
        guard
            let raw = message[TrackingCommand.messageKey] as? String,
            let action = TrackingCommand.Action(rawValue: raw)
        else {
            return nil
        }
        return TrackingCommand(action: action)
    }
}
