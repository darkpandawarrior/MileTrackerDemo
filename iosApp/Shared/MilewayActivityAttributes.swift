// P6.4: ActivityKit contract for the tracking Live Activity + Dynamic Island. Shared (not
// framework-backed, same reasoning as MilewaySyncModels.swift) so both the host app (starts/updates/
// ends the Activity) and the MilewayWidgets extension (renders it) compile the exact same
// `ActivityAttributes` type without linking the KMP `Mileway` framework from the extension process.
import ActivityKit
import Foundation

@available(iOS 16.2, *)
struct MilewayTrackingAttributes: ActivityAttributes {
    /// Fields that change while the trip is tracking — ActivityKit re-renders on every update.
    struct ContentState: Codable, Hashable {
        var distanceKm: Double
        var elapsedSeconds: Int
        var isPaused: Bool
    }

    /// Fixed for the lifetime of one Live Activity (set at `Activity.request`, never updated).
    var tripStartedAtMs: Int64
}
