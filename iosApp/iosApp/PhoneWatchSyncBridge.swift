// P4.5: phone-side half of the WatchConnectivity sync — mirrors
// `iosApp/MilewayWatch/WatchSyncBridge.swift`. Publishes the latest snapshot via
// `updateApplicationContext` (coalesced latest-state, same semantics as the Android
// `WearDataLayerWatchSyncBridge`/`WearDataLayerSyncBridge` pair, P2.9), so the watch always has
// fresh data offline without the phone needing to be reachable at watch-render time.
//
// Z.5d: also the phone-side receiver for the reverse, watch->phone start/stop-tracking command
// (`WatchTrackingCommandSender`, `iosApp/MilewayWatch/WatchTrackingCommandSender.swift`) — the
// watchOS analogue of Android's `WearTrackingCommandService` (`app/src/gms/kotlin/com/mileway/
// platform/gms/WearTrackingCommandService.kt`). Decodes the message and dispatches straight to
// `IosIntentEntry`, the same shared KMP seam iOS's own App Intents (P7.1) already bind to — no new
// domain logic, no network.

import Foundation
import WatchConnectivity
import Mileway

final class PhoneWatchSyncBridge: NSObject, WCSessionDelegate {
    static let shared = PhoneWatchSyncBridge()

    func activate() {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
    }

    func session(
        _ session: WCSession,
        didReceiveMessage message: [String: Any],
        replyHandler: @escaping ([String: Any]) -> Void
    ) {
        handleTrackCommand(message)
        replyHandler([:])
    }

    func session(_ session: WCSession, didReceiveMessage message: [String: Any]) {
        handleTrackCommand(message)
    }

    private func handleTrackCommand(_ message: [String: Any]) {
        guard
            let raw = message[trackCommandMessageKey] as? [String: Any],
            let command = TrackingCommandCodec.decode(raw)
        else {
            return
        }
        Task { @MainActor in
            switch command.action {
            case .start:
                try? await IosIntentEntry.shared.startTracking()
            case .stop:
                try? await IosIntentEntry.shared.stopTracking()
            }
        }
    }

    /// Pushes the latest snapshot to the paired watch. A no-op if no watch is paired/reachable —
    /// `applicationContext` still delivers on next watch wake, it just can't send while inactive.
    func push(_ payload: MilewaySyncPayload) {
        // P6.4: same snapshot drives the Live Activity — start/update/end tracks isTracking edges.
        if #available(iOS 16.2, *) {
            TrackingLiveActivityController.shared.apply(payload)
        }
        guard WCSession.isSupported(), WCSession.default.activationState == .activated else { return }
        try? WCSession.default.updateApplicationContext(MilewaySyncCodec.encode(payload))
    }

    // No-op WCSessionDelegate callbacks the phone side doesn't act on but must implement.
    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {}

    func sessionDidBecomeInactive(_ session: WCSession) {}

    func sessionDidDeactivate(_ session: WCSession) {
        // Re-activate for a newly paired watch, per Apple's guidance.
        WCSession.default.activate()
    }
}
