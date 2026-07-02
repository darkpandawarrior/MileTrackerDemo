// P7.1: registers Mileway's App Intents as system App Shortcuts — this is what actually makes
// them appear (with no per-user setup) in Spotlight, the Shortcuts app, and as Siri phrases.
// Siri phrase invocation itself is device-gated (needs a real device/simulator with Siri, not
// gradle/xcodebuild) — this only proves registration + compile, per P7.1's acceptance.

import AppIntents

struct MilewayShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: StartTrackingIntent(),
            phrases: [
                "Start a trip in \(.applicationName)",
                "Start tracking in \(.applicationName)",
            ],
            shortTitle: "Start Trip",
            systemImageName: "location.fill"
        )
        AppShortcut(
            intent: StopTrackingIntent(),
            phrases: [
                "Stop my trip in \(.applicationName)",
                "Stop tracking in \(.applicationName)",
            ],
            shortTitle: "Stop Trip",
            systemImageName: "stop.fill"
        )
        AppShortcut(
            intent: LogExpenseIntent(),
            phrases: [
                "Log an expense in \(.applicationName)",
                "Add an expense in \(.applicationName)",
            ],
            shortTitle: "Log Expense",
            systemImageName: "indianrupeesign.circle.fill"
        )
    }
}
