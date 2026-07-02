// P4.4: the watch dashboard — today/week distance + tracking pill, rendered from
// WatchDashboardModel's published WatchSnapshot. Colors mirror core:ui's Matrix token spec
// (canvas #010701, phosphor accent #00FF41) natively in SwiftUI — Compose tokens don't cross
// the KMP/SwiftUI boundary, so the palette is re-declared here, not imported.

import SwiftUI

/// The Matrix palette's watch-relevant subset, hand-mirrored from
/// `core/ui/.../theme/MilewayThemes.kt`'s `MatrixSpec` (accent `#00FF41`, canvas `#010701`).
enum WatchMatrixPalette {
    static let canvas = Color(red: 0x01 / 255, green: 0x07 / 255, blue: 0x01 / 255)
    static let accent = Color(red: 0x00 / 255, green: 0xFF / 255, blue: 0x41 / 255)
    static let text = Color(red: 0xB8 / 255, green: 0xFF / 255, blue: 0xCC / 255)
    static let textMuted = Color(red: 0x3A / 255, green: 0x66 / 255, blue: 0x45 / 255)
}

struct WatchDashboardView: View {
    @ObservedObject var model: WatchDashboardModel

    var body: some View {
        ScrollView {
            VStack(spacing: 6) {
                Text(String(format: "%.1f km", model.snapshot.todayDistanceKm))
                    .font(.system(size: 28, weight: .bold, design: .rounded))
                    .foregroundStyle(WatchMatrixPalette.accent)
                Text("today")
                    .font(.caption2)
                    .foregroundStyle(WatchMatrixPalette.textMuted)

                Text(String(format: "%.1f km this week", model.snapshot.weekDistanceKm))
                    .font(.caption)
                    .foregroundStyle(WatchMatrixPalette.text)

                trackingPill

                NavigationLink("Trips") {
                    WatchTripListView(trips: model.trips)
                }
                .padding(.top, 4)
            }
            .padding()
        }
        .background(WatchMatrixPalette.canvas)
        .navigationTitle("Mileway")
        .task { await model.refresh() }
    }

    private var trackingPill: some View {
        Text(pillLabel)
            .font(.caption2.weight(.semibold))
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(pillColor.opacity(0.2))
            .foregroundStyle(pillColor)
            .clipShape(Capsule())
    }

    private var pillLabel: String {
        if model.snapshot.isTracking {
            return model.snapshot.isPaused ? "Paused" : "Tracking"
        }
        return "Idle"
    }

    private var pillColor: Color {
        model.snapshot.isTracking && !model.snapshot.isPaused ? WatchMatrixPalette.accent : WatchMatrixPalette.textMuted
    }
}

#Preview {
    NavigationStack {
        WatchDashboardView(model: WatchDashboardModel())
    }
}
