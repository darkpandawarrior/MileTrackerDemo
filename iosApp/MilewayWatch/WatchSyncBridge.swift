// P4.5: watch-side half of the WatchConnectivity sync. `applicationContext` is coalesced
// latest-state transport (not a queued message) — exactly the same "always current, never a
// backlog" semantics as the Android Data Layer `PutDataMapRequest` bridge
// (`wear/src/gms/kotlin/com/mileway/wear/gms/WearDataLayerSyncBridge.kt`) this mirrors. The phone
// publishes on every `SurfaceSnapshot` change; the watch only ever needs the newest value.

import Foundation
import WatchConnectivity

/// Republishes the latest `MilewaySyncPayload` pushed from the phone. `WatchDashboardModel` (or a
/// future observer) reads `latest` for first paint and can `Task`-await `updates` for live pushes —
/// mirrors `WatchSyncBridge.latest()`/`observeIncoming()` on the Kotlin side (P1.3) without needing
/// the KMP interface itself, since this side never touches `:sharedWatch`.
final class WatchSyncBridge: NSObject, WCSessionDelegate {
    static let shared = WatchSyncBridge()

    private(set) var latest: MilewaySyncPayload?
    private let onUpdate: (MilewaySyncPayload) -> Void

    init(onUpdate: @escaping (MilewaySyncPayload) -> Void = { _ in }) {
        self.onUpdate = onUpdate
        super.init()
    }

    func activate() {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
        if let payload = MilewaySyncCodec.decode(session.receivedApplicationContext) {
            latest = payload
        }
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        if let payload = MilewaySyncCodec.decode(session.receivedApplicationContext) {
            latest = payload
            onUpdate(payload)
        }
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        guard let payload = MilewaySyncCodec.decode(applicationContext) else { return }
        DispatchQueue.main.async { [self] in
            latest = payload
            onUpdate(payload)
        }
    }

    #if os(iOS)
    // Required by WCSessionDelegate on iOS (a single watchOS app has no multi-session concept,
    // so these are no-ops there); required unconditionally on iOS regardless of watch pairing state.
    func sessionDidBecomeInactive(_ session: WCSession) {}

    func sessionDidDeactivate(_ session: WCSession) {
        WCSession.default.activate()
    }
    #endif
}
