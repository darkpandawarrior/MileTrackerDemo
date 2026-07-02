// P4.3: main-actor presentation model for the watch dashboard. Talks to the KMP-backed
// `MilewayWatchGraph` actor and republishes plain Swift state SwiftUI can bind to directly.

import Foundation

private let recentTripsLimit: Int32 = 5

@MainActor
final class WatchDashboardModel: ObservableObject {
    @Published private(set) var snapshot: WatchSnapshot
    @Published private(set) var trips: [WatchTrip] = []

    private let graph: WatchGraphProviding

    /// `cachedSnapshot` seeds `@Published` state before the first `refresh()` completes, so the
    /// dashboard's first paint already has something to show instead of flashing zeroes.
    init(graph: WatchGraphProviding = MilewayWatchGraph(), cachedSnapshot: WatchSnapshot = .empty) {
        self.graph = graph
        self.snapshot = cachedSnapshot
    }

    func refresh() async {
        snapshot = await graph.currentSnapshot()
        trips = await graph.recentTrips(limit: recentTripsLimit)
    }
}
