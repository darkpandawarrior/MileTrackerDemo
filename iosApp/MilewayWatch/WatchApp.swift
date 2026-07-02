// MilewayWatch: watchOS companion app.
// Presents today's tracked distance and recent trips on the wrist, backed by WatchDashboardModel
// which reads through MilewayWatchGraph (P4.3) into the `:sharedWatch` KMP framework's
// WatchDomainFacade. WatchRootView (P4.4) owns the NavigationStack + dashboard/trip-list screens.

import SwiftUI

@main
struct MilewayWatchApp: App {
    init() {
        // P4.5: activate WatchConnectivity so applicationContext pushes from the phone arrive
        // even before the KMP-backed dashboard has completed its own first refresh.
        WatchSyncBridge.shared.activate()
    }

    var body: some Scene {
        WindowGroup {
            WatchRootView()
        }
    }
}
