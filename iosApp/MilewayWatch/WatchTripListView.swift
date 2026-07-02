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
                                .lineLimit(2)
                            Text(String(format: "%.1f km", trip.km))
                                .font(.caption2)
                                .foregroundStyle(WatchMatrixPalette.textMuted)
                        }
                    }
                    // P8.2: one VoiceOver stop per row ("Commute, 8.2 kilometers") instead of two
                    // fragments read back to back.
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel(trip.label.isEmpty ? "Trip" : trip.label)
                    .accessibilityValue(String(format: "%.1f kilometers", trip.km))
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
                .minimumScaleFactor(0.7)
                .lineLimit(2)
            Text(String(format: "%.1f km", trip.km))
                .font(.system(size: 24, weight: .bold, design: .rounded))
                .foregroundStyle(WatchMatrixPalette.accent)
                .minimumScaleFactor(0.6)
                .lineLimit(1)
        }
        .padding()
        .navigationTitle("Detail")
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(trip.label.isEmpty ? "Trip" : trip.label)
        .accessibilityValue(String(format: "%.1f kilometers", trip.km))
    }
}

#Preview {
    NavigationStack {
        WatchTripListView(trips: [
            WatchTrip(id: "1", label: "Commute", km: 8.2, endMs: 0),
        ])
    }
}
