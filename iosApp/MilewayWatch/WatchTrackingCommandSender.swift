// Z.5d: the watch's half of the watch->phone start/stop-tracking command — the watchOS analogue
// of Wear's `WearTrackingCommandSender` (`wear/src/gms/kotlin/com/mileway/wear/gms/
// WearTrackingCommandSender.kt`). `:sharedWatch`'s `WatchDomainFacade` is read-only (P3.3's
// documented scope cut: `TrackingController` start/stop is a `feature:tracking` type, and
// `feature:tracking` applies Compose Multiplatform, which has no watchOS target — PLAN_V23 §6).
// Rather than carving a watchos-safe command slice out of `feature:tracking`, this sends a plain
// `WCSession` message the phone's `PhoneWatchSyncBridge` decodes and dispatches to the ALREADY
// shared `IosIntentEntry.startTracking()/stopTracking()` (the same seam iOS's own App Intents use,
// `iosApp/iosApp/intents/StartTrackingIntent.swift`) — no network, no new phone-side domain logic.
import Foundation
import WatchConnectivity

/// Message path both this sender and the phone's `PhoneWatchSyncBridge` (`iosApp/iosApp/
/// PhoneWatchSyncBridge.swift`) use — deliberately distinct from `MilewaySyncPayload`'s
/// `applicationContext` channel (that one is coalesced latest-state; a command must not be
/// coalesced away).
let trackCommandMessageKey = "com.mileway.track-command"

/// Sends a watch-initiated start/stop command to the paired phone via `WCSession.sendMessage`.
/// A no-op (never throws) if no phone is reachable, mirroring `WearTrackingCommandSender`'s
/// `runCatching` swallow-and-log behavior — the watch dashboard has no error UI for this.
final class WatchTrackingCommandSender {
    static let shared = WatchTrackingCommandSender()

    /// Sends a start command to the paired phone, if one is reachable.
    func sendStart() { send(TrackingCommand(action: .start)) }

    /// Sends a stop command to the paired phone, if one is reachable.
    func sendStop() { send(TrackingCommand(action: .stop)) }

    private func send(_ command: TrackingCommand) {
        guard WCSession.isSupported(), WCSession.default.activationState == .activated else { return }
        guard WCSession.default.isReachable else { return }
        let message = [trackCommandMessageKey: TrackingCommandCodec.encode(command)]
        WCSession.default.sendMessage(message, replyHandler: nil) { _ in
            // Best-effort: dropped commands aren't retried, matching Wear's swallow-and-log.
        }
    }
}
