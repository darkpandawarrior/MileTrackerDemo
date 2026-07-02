// P4.3: the Swift-visible composition root for the watchOS target.
// Wraps `WatchFacadeFactory` (KMP `:sharedWatch`, watchosMain) behind a Swift `actor` so the
// Kotlin/Native `WatchDomainFacade` — and the Room database it opens — is only ever touched from
// a single serial context, then republishes plain Swift value types the SwiftUI layer can render
// without reaching back into the Kotlin framework.

import SharedWatch

/// Plain Swift mirror of `core:data`'s `SurfaceSnapshot` — only the fields the watch dashboard renders.
struct WatchSnapshot: Equatable {
    let todayDistanceKm: Double
    let weekDistanceKm: Double
    let weekGoalProgress: Double
    let isTracking: Bool
    let isPaused: Bool

    static let empty = WatchSnapshot(
        todayDistanceKm: 0.0,
        weekDistanceKm: 0.0,
        weekGoalProgress: 0.0,
        isTracking: false,
        isPaused: false,
    )
}

/// Plain Swift mirror of `:sharedWatch`'s `WatchTripSummary`.
struct WatchTrip: Equatable, Identifiable {
    let id: String
    let label: String
    let km: Double
    let endMs: Int64
}

/// Protocol over the KMP-facing surface `MilewayWatchGraph` needs — lets `WatchDashboardModel`
/// tests substitute a fake in place of the real `WatchDomainFacade` without touching Room.
protocol WatchGraphProviding {
    func currentSnapshot() async -> WatchSnapshot
    func recentTrips(limit: Int32) async -> [WatchTrip]
}

/// Serial-isolated owner of the KMP `WatchDomainFacade`. `actor` guarantees the Kotlin/Native
/// object (and the Room database underneath it) is only ever entered from one thread at a time,
/// matching Kotlin/Native's "one thread owns an object graph" concurrency story.
actor MilewayWatchGraph: WatchGraphProviding {
    private let facade: WatchDomainFacade = WatchFacadeFactory.shared.create()

    /// Latest published snapshot, mapped to the plain Swift shape.
    func currentSnapshot() async -> WatchSnapshot {
        guard let snapshot: SurfaceSnapshot = await firstValue(of: facade.observeSnapshot()) else {
            return .empty
        }
        return Self.toWatchSnapshot(snapshot)
    }

    /// Most recent completed trips, newest first.
    func recentTrips(limit: Int32) async -> [WatchTrip] {
        guard let trips: [WatchTripSummary] = await firstValue(of: facade.recentTrips(limit: limit)) else {
            return []
        }
        return trips.map(Self.toWatchTrip)
    }

    private static func toWatchSnapshot(_ snapshot: SurfaceSnapshot) -> WatchSnapshot {
        WatchSnapshot(
            todayDistanceKm: snapshot.todayDistanceKm,
            weekDistanceKm: snapshot.weekDistanceKm,
            weekGoalProgress: Double(snapshot.weekGoalProgress),
            isTracking: snapshot.isTracking,
            isPaused: snapshot.isPaused,
        )
    }

    private static func toWatchTrip(_ trip: WatchTripSummary) -> WatchTrip {
        WatchTrip(id: trip.id, label: trip.label, km: trip.km, endMs: trip.endMs)
    }
}

/// Awaits the first element of a Kotlin `Flow` exported to Swift, or `nil` if the flow completes
/// with no elements (or errors). `Flow`'s ObjC export is completion-handler based, not `async` —
/// this bridges it to `async/await` with a one-shot continuation, discarding the value's static
/// type via `Any` since Kotlin `Flow<T>` erases `T` at the ObjC boundary.
private func firstValue<T>(of flow: Kotlinx_coroutines_coreFlow) async -> T? {
    await withCheckedContinuation { (continuation: CheckedContinuation<T?, Never>) in
        var resumed = false
        let collector = SingleShotFlowCollector { value in
            guard !resumed else { return }
            resumed = true
            continuation.resume(returning: value as? T)
        }
        flow.collect(collector: collector) { _ in
            guard !resumed else { return }
            resumed = true
            continuation.resume(returning: nil)
        }
    }
}

/// Minimal `FlowCollector` adapter — forwards the first emitted value to a Swift closure, then
/// stops caring about further emissions (the underlying Kotlin coroutine still runs to its own
/// completion, which fires [firstValue]'s completion handler harmlessly as a no-op by then).
private final class SingleShotFlowCollector: NSObject, Kotlinx_coroutines_coreFlowCollector {
    private let onValue: (Any?) -> Void

    init(onValue: @escaping (Any?) -> Void) {
        self.onValue = onValue
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        onValue(value)
        completionHandler(nil)
    }
}
