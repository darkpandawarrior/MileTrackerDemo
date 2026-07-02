import SwiftUI

struct ContentView: View {
    @StateObject private var model = WatchDashboardModel()

    var body: some View {
        VStack(spacing: 8) {
            Text(String(format: "%.1f km", model.snapshot.todayDistanceKm))
                .font(.system(size: 28, weight: .bold, design: .rounded))

            Text(model.snapshot.isTracking ? "Tracking" : "Idle")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .task { await model.refresh() }
    }
}
