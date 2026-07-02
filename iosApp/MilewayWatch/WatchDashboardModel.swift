// P4.3: main-actor presentation model for the watch dashboard. Talks to the KMP-backed
// `MilewayWatchGraph` actor and republishes plain Swift state SwiftUI can bind to directly.
//
// Z.5d: also owns the watch->phone start/stop command send (`WatchTrackingCommandSender`) — the
// dashboard's tracking pill becomes tappable, mirroring Wear's ongoing-activity start/stop wiring
// (P2.8/`WearViewModel`). The command is fire-and-forget over WCSession; the pill's own tracking
// state still comes from `refresh()`'s `SurfaceSnapshot` (pushed back via P4.5's applicationContext
// sync once the phone acts on the command), not from this call's local success.

import Foundation

private let recentTripsLimit: Int32 = 5

/// Narrow seam over `WatchTrackingCommandSender` so tests can substitute a fake without touching
/// `WCSession`.
protocol TrackingCommandSending {
    func sendStart()
    func sendStop()
}

extension WatchTrackingCommandSender: TrackingCommandSending {}

@MainActor
final class WatchDashboardModel: ObservableObject {
    @Published private(set) var snapshot: WatchSnapshot
    @Published private(set) var trips: [WatchTrip] = []

    private let graph: WatchGraphProviding
    private let commandSender: TrackingCommandSending

    /// `cachedSnapshot` seeds `@Published` state before the first `refresh()` completes, so the
    /// dashboard's first paint already has something to show instead of flashing zeroes.
    init(
        graph: WatchGraphProviding = MilewayWatchGraph(),
        commandSender: TrackingCommandSending = WatchTrackingCommandSender.shared,
        cachedSnapshot: WatchSnapshot = .empty
    ) {
        self.graph = graph
        self.commandSender = commandSender
        self.snapshot = cachedSnapshot
    }

    func refresh() async {
        snapshot = await graph.currentSnapshot()
        trips = await graph.recentTrips(limit: recentTripsLimit)
    }

    /// Toggles tracking on the paired phone: sends stop if currently tracking, start otherwise.
    func toggleTracking() {
        if snapshot.isTracking {
            commandSender.sendStop()
        } else {
            commandSender.sendStart()
        }
    }
}
