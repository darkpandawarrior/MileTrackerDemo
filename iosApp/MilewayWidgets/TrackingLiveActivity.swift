// P6.4: ActivityKit widget — Lock Screen banner + Dynamic Island (compact/minimal/expanded)
// presentations for an in-progress trip. Reads only the ContentState ActivityKit hands it (pushed
// by the host app's TrackingLiveActivityController) — no snapshot-store read needed here, unlike
// the home/lock-screen widgets in MileageWidgetProvider.swift, since ActivityKit itself delivers
// state updates to this process.

import ActivityKit
import SwiftUI
import WidgetKit

private func formattedElapsed(_ seconds: Int) -> String {
    let m = seconds / 60
    let s = seconds % 60
    return String(format: "%d:%02d", m, s)
}

@available(iOS 16.2, *)
struct TrackingLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: MilewayTrackingAttributes.self) { context in
            // Lock Screen / banner presentation.
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(context.state.isPaused ? "Paused" : "Tracking")
                        .font(.caption2)
                        .foregroundStyle(WidgetMatrixPalette.textMuted)
                    Text(String(format: "%.1f km", context.state.distanceKm))
                        .font(.title2.bold())
                        .foregroundStyle(WidgetMatrixPalette.accent)
                }
                Spacer()
                Text(formattedElapsed(context.state.elapsedSeconds))
                    .font(.title3.monospacedDigit())
                    .foregroundStyle(WidgetMatrixPalette.text)
            }
            .padding()
            .activityBackgroundTint(WidgetMatrixPalette.canvas)
            .activitySystemActionForegroundColor(WidgetMatrixPalette.text)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Text(String(format: "%.1f km", context.state.distanceKm))
                        .font(.headline)
                        .foregroundStyle(WidgetMatrixPalette.accent)
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text(formattedElapsed(context.state.elapsedSeconds))
                        .font(.headline.monospacedDigit())
                        .foregroundStyle(WidgetMatrixPalette.text)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Text(context.state.isPaused ? "Paused" : "Tracking trip")
                        .font(.caption)
                        .foregroundStyle(WidgetMatrixPalette.textMuted)
                }
            } compactLeading: {
                Image(systemName: "location.fill")
                    .foregroundStyle(WidgetMatrixPalette.accent)
            } compactTrailing: {
                Text(formattedElapsed(context.state.elapsedSeconds))
                    .font(.caption2.monospacedDigit())
                    .foregroundStyle(WidgetMatrixPalette.text)
            } minimal: {
                Image(systemName: "location.fill")
                    .foregroundStyle(WidgetMatrixPalette.accent)
            }
        }
    }
}
