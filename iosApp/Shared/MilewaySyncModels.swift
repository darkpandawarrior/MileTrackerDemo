// P4.5: the Swift analogue of `core:data`'s `WatchSyncPayload` (see
// `core/data/src/commonMain/kotlin/com/mileway/core/data/watch/WatchSyncPayload.kt`) — same field
// names/order so the JSON either side encodes decodes cleanly on the other, even though the phone
// target talks to `:sharedWatch`'s KMP `WatchSyncPayload` (via the shared framework) and the watch
// target only ever sees this plain Swift `Codable` struct. Compiled into BOTH the `iosApp` and
// `MilewayWatch` targets (see `iosApp/project.yml`) so `WCSession.updateApplicationContext` on one
// side and the delegate callback on the other decode the exact same shape without duplicating it.
//
// Deliberately NOT `Mileway`/`SharedWatch`-framework-backed: `WCSession` payload dictionaries are
// plain `[String: Any]`, and keeping the Codable model framework-free means the watch target (which
// only links `SharedWatch`, not the phone's `Mileway` framework) can decode a context pushed by the
// phone without any cross-framework dependency.
import Foundation

struct MilewaySyncPayload: Codable, Equatable {
    var todayKm: Double = 0.0
    var weekKm: Double = 0.0
    var tripCount: Int = 0
    var isTracking: Bool = false
    var isPaused: Bool = false
    var weekGoalProgress: Double = 0.0
    var lastTripLabel: String?
    var updatedAtMs: Int64 = 0

    static let empty = MilewaySyncPayload()
}

/// `WCSession.updateApplicationContext` takes `[String: Any]`, not `Data` — encode/decode through
/// `JSONEncoder`/`JSONDecoder` into a `[String: Any]` dictionary via an intermediate JSON object,
/// so both sides can reuse `Codable` instead of hand-rolling dictionary keys twice.
enum MilewaySyncCodec {
    private static let encoder = JSONEncoder()
    private static let decoder = JSONDecoder()

    static func encode(_ payload: MilewaySyncPayload) -> [String: Any] {
        guard
            let data = try? encoder.encode(payload),
            let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return [:]
        }
        return object
    }

    /// Returns `nil` (rather than throwing) on any malformed/legacy-shape context, mirroring
    /// `SnapshotCacheCodec.decode`'s fail-soft contract on the Kotlin side.
    static func decode(_ context: [String: Any]) -> MilewaySyncPayload? {
        guard
            let data = try? JSONSerialization.data(withJSONObject: context),
            let payload = try? decoder.decode(MilewaySyncPayload.self, from: data)
        else {
            return nil
        }
        return payload
    }
}
