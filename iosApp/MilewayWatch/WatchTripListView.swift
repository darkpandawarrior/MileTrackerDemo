// P4.4: recent-trips list + detail, rendered from WatchDashboardModel's published [WatchTrip]
// (sourced from WatchDomainFacade.recentTrips via MilewayWatchGraph).

import SwiftUI

struct WatchTripListView: View {
    let trips: [WatchTrip]

    var body: some View {
        Group {
            if trips.isEmpty {
                Text("No trips yet")
                    .font(.caption)
                    .foregroundStyle(WatchMatrixPalette.textMuted)
            } else {
                List(trips) { trip in
                    NavigationLink {
                        WatchTripDetailView(trip: trip)
                    } label: {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(trip.label.isEmpty ? "Trip" : trip.label)
                                .font(.body)
                            Text(String(format: "%.1f km", trip.km))
                                .font(.caption2)
                                .foregroundStyle(WatchMatrixPalette.textMuted)
                        }
                    }
                }
            }
        }
        .navigationTitle("Trips")
    }
}

struct WatchTripDetailView: View {
    let trip: WatchTrip

    var body: some View {
        VStack(spacing: 8) {
            Text(trip.label.isEmpty ? "Trip" : trip.label)
                .font(.headline)
            Text(String(format: "%.1f km", trip.km))
                .font(.system(size: 24, weight: .bold, design: .rounded))
                .foregroundStyle(WatchMatrixPalette.accent)
        }
        .padding()
        .navigationTitle("Detail")
    }
}

#Preview {
    NavigationStack {
        WatchTripListView(trips: [
            WatchTrip(id: "1", label: "Commute", km: 8.2, endMs: 0),
        ])
    }
}
