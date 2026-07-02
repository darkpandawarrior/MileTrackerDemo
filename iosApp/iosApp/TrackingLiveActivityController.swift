// P6.4: host-app-side ActivityKit lifecycle (start/update/end) for the tracking Live Activity.
// Driven from the same `MilewaySyncPayload` the watch sync bridge already pushes on every trip
// change (see PhoneWatchSyncBridge.swift) — no separate tracking-state source, same "one snapshot,
// many readers" shape as P6.1's SnapshotCache.
import ActivityKit
import Foundation

@available(iOS 16.2, *)
final class TrackingLiveActivityController {
    static let shared = TrackingLiveActivityController()

    private var currentActivity: Activity<MilewayTrackingAttributes>?

    /// Call on every snapshot update (mirrors `PhoneWatchSyncBridge.push`): starts the Activity on
    /// the tracking-off→on edge, updates it while tracking, ends it on the on→off edge. A no-op if
    /// Live Activities are disabled by the user (`areActivitiesEnabled`) or unsupported.
    func apply(_ payload: MilewaySyncPayload) {
        guard ActivityAuthorizationInfo().areActivitiesEnabled else { return }

        if !payload.isTracking {
            end()
            return
        }

        let elapsedSeconds = max(0, Int((Date().timeIntervalSince1970 * 1000 - Double(payload.updatedAtMs)) / 1000))
        let state = MilewayTrackingAttributes.ContentState(
            distanceKm: payload.todayKm,
            elapsedSeconds: elapsedSeconds,
            isPaused: payload.isPaused
        )

        if let activity = currentActivity {
            Task { await activity.update(ActivityContent(state: state, staleDate: nil)) }
        } else {
            let attributes = MilewayTrackingAttributes(tripStartedAtMs: payload.updatedAtMs)
            currentActivity = try? Activity.request(
                attributes: attributes,
                content: ActivityContent(state: state, staleDate: nil)
            )
        }
    }

    private func end() {
        guard let activity = currentActivity else { return }
        currentActivity = nil
        Task { await activity.end(nil, dismissalPolicy: .immediate) }
    }
}
