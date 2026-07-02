// P4.5: phone-side half of the WatchConnectivity sync — mirrors
// `iosApp/MilewayWatch/WatchSyncBridge.swift`. Publishes the latest snapshot via
// `updateApplicationContext` (coalesced latest-state, same semantics as the Android
// `WearDataLayerWatchSyncBridge`/`WearDataLayerSyncBridge` pair, P2.9), so the watch always has
// fresh data offline without the phone needing to be reachable at watch-render time.

import Foundation
import WatchConnectivity

final class PhoneWatchSyncBridge: NSObject, WCSessionDelegate {
    static let shared = PhoneWatchSyncBridge()

    func activate() {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
    }

    /// Pushes the latest snapshot to the paired watch. A no-op if no watch is paired/reachable —
    /// `applicationContext` still delivers on next watch wake, it just can't send while inactive.
    func push(_ payload: MilewaySyncPayload) {
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
