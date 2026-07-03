// P4.3 acceptance: a small XCTest on WatchDashboardModel against a fake WatchGraphProviding —
// proves the model populates its @Published state from refresh() without touching the real
// Kotlin/Native framework (no Room database, no watchOS-only WatchFacadeFactory).

import XCTest
@testable import MilewayWatch

private actor FakeWatchGraph: WatchGraphProviding {
    private let snapshotToReturn: WatchSnapshot
    private let tripsToReturn: [WatchTrip]

    init(snapshot: WatchSnapshot, trips: [WatchTrip]) {
        snapshotToReturn = snapshot
        tripsToReturn = trips
    }

    func currentSnapshot() async -> WatchSnapshot { snapshotToReturn }

    func recentTrips(limit: Int32) async -> [WatchTrip] { Array(tripsToReturn.prefix(Int(limit))) }
}

/// Z.5d: records which command `WatchDashboardModel.toggleTracking()` sent, without touching
/// `WCSession`.
private final class FakeTrackingCommandSender: TrackingCommandSending {
    private(set) var startCount = 0
    private(set) var stopCount = 0

    func sendStart() { startCount += 1 }
    func sendStop() { stopCount += 1 }
}

final class WatchDashboardModelTests: XCTestCase {
    @MainActor
    func testRefreshPopulatesSnapshotFromGraph() async {
        let snapshot = WatchSnapshot(
            todayDistanceKm: 12.5,
            weekDistanceKm: 40.0,
            weekGoalProgress: 0.5,
            isTracking: true,
            isPaused: false,
        )
        let model = WatchDashboardModel(graph: FakeWatchGraph(snapshot: snapshot, trips: []))

        await model.refresh()

        XCTAssertEqual(model.snapshot, snapshot)
    }

    @MainActor
    func testRefreshPopulatesTripsFromGraph() async {
        let trips = [WatchTrip(id: "a", label: "Commute", km: 8.2, endMs: 100)]
        let model = WatchDashboardModel(graph: FakeWatchGraph(snapshot: .empty, trips: trips))

        await model.refresh()

        XCTAssertEqual(model.trips, trips)
    }

    @MainActor
    func testInitialStateUsesCachedSnapshotBeforeRefresh() async {
        let cached = WatchSnapshot(
            todayDistanceKm: 3.0,
            weekDistanceKm: 10.0,
            weekGoalProgress: 0.1,
            isTracking: false,
            isPaused: false,
        )
        let model = WatchDashboardModel(graph: FakeWatchGraph(snapshot: .empty, trips: []), cachedSnapshot: cached)

        XCTAssertEqual(model.snapshot, cached)
    }

    @MainActor
    func testToggleTrackingSendsStartWhenIdle() {
        let sender = FakeTrackingCommandSender()
        let idle = WatchSnapshot(todayDistanceKm: 0, weekDistanceKm: 0, weekGoalProgress: 0, isTracking: false, isPaused: false)
        let model = WatchDashboardModel(
            graph: FakeWatchGraph(snapshot: .empty, trips: []),
            commandSender: sender,
            cachedSnapshot: idle
        )

        model.toggleTracking()

        XCTAssertEqual(sender.startCount, 1)
        XCTAssertEqual(sender.stopCount, 0)
    }

    @MainActor
    func testToggleTrackingSendsStopWhenTracking() {
        let sender = FakeTrackingCommandSender()
        let tracking = WatchSnapshot(todayDistanceKm: 5, weekDistanceKm: 5, weekGoalProgress: 0.1, isTracking: true, isPaused: false)
        let model = WatchDashboardModel(
            graph: FakeWatchGraph(snapshot: .empty, trips: []),
            commandSender: sender,
            cachedSnapshot: tracking
        )

        model.toggleTracking()

        XCTAssertEqual(sender.stopCount, 1)
        XCTAssertEqual(sender.startCount, 0)
    }
}
