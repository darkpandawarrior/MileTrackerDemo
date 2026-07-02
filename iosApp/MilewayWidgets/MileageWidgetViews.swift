// P6.3: widget view family — home screen (systemSmall/systemMedium) + Lock Screen accessories
// (accessoryRectangular/accessoryCircular). Palette hand-mirrored from core:ui's Matrix token spec,
// same approach WatchDashboardView.swift already takes for the watch target (Compose tokens don't
// cross the KMP/SwiftUI boundary).

import SwiftUI
import WidgetKit

enum WidgetMatrixPalette {
    static let canvas = Color(red: 0x01 / 255, green: 0x07 / 255, blue: 0x01 / 255)
    static let accent = Color(red: 0x00 / 255, green: 0xFF / 255, blue: 0x41 / 255)
    static let text = Color(red: 0xB8 / 255, green: 0xFF / 255, blue: 0xCC / 255)
    static let textMuted = Color(red: 0x3A / 255, green: 0x66 / 255, blue: 0x45 / 255)
}

private func formattedKm(_ value: Double) -> String {
    String(format: "%.1f km", value)
}

// MARK: - Home screen (systemSmall / systemMedium)

struct MileageHomeWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: MileageWidgetEntry

    var body: some View {
        ZStack {
            WidgetMatrixPalette.canvas
            VStack(alignment: .leading, spacing: 4) {
                Text(formattedKm(entry.payload.todayKm))
                    .font(.system(size: 22, weight: .bold, design: .rounded))
                    .foregroundStyle(WidgetMatrixPalette.accent)
                Text("today")
                    .font(.caption2)
                    .foregroundStyle(WidgetMatrixPalette.textMuted)

                if family == .systemMedium {
                    Text("\(formattedKm(entry.payload.weekKm)) this week · \(entry.payload.tripCount) trips")
                        .font(.caption)
                        .foregroundStyle(WidgetMatrixPalette.text)
                }

                Spacer(minLength: 4)

                if #available(iOS 17.0, *) {
                    Button(intent: ToggleTrackingIntent()) {
                        Text(entry.payload.isTracking ? "Stop" : "Start")
                            .font(.caption2.weight(.semibold))
                    }
                    .tint(WidgetMatrixPalette.accent)
                } else {
                    Text(entry.payload.isTracking ? "Tracking" : "Idle")
                        .font(.caption2.weight(.semibold))
                        .foregroundStyle(WidgetMatrixPalette.textMuted)
                }
            }
            .padding()
        }
    }
}

// MARK: - Lock Screen accessories

struct MileageAccessoryRectangularView: View {
    let entry: MileageWidgetEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(formattedKm(entry.payload.todayKm))
                .font(.headline)
            Text(entry.payload.isTracking ? "Tracking" : "Idle")
                .font(.caption2)
        }
    }
}

struct MileageAccessoryCircularView: View {
    let entry: MileageWidgetEntry

    var body: some View {
        Gauge(value: min(entry.payload.weekGoalProgress, 1.0)) {
            Text("km")
        } currentValueLabel: {
            Text(String(format: "%.0f", entry.payload.todayKm))
        }
        .gaugeStyle(.accessoryCircular)
    }
}

// MARK: - Widgets

struct MileageHomeWidget: Widget {
    let kind = "MileageHomeWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: MileageWidgetProvider()) { entry in
            if #available(iOS 17.0, *) {
                MileageHomeWidgetView(entry: entry)
                    .containerBackground(WidgetMatrixPalette.canvas, for: .widget)
            } else {
                MileageHomeWidgetView(entry: entry)
                    .background(WidgetMatrixPalette.canvas)
            }
        }
        .configurationDisplayName("Mileway")
        .description("Today's and this week's distance, with quick start/stop.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

private struct MileageAccessoryView: View {
    @Environment(\.widgetFamily) private var family
    let entry: MileageWidgetEntry

    var body: some View {
        switch family {
        case .accessoryCircular:
            MileageAccessoryCircularView(entry: entry)
        default:
            MileageAccessoryRectangularView(entry: entry)
        }
    }
}

struct MileageLockScreenWidget: Widget {
    let kind = "MileageLockScreenWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: MileageWidgetProvider()) { entry in
            MileageAccessoryView(entry: entry)
        }
        .configurationDisplayName("Mileway Today")
        .description("Today's distance on your Lock Screen.")
        .supportedFamilies([.accessoryRectangular, .accessoryCircular])
    }
}
