// MilewayWatch: watchOS companion app.
// Presents today's tracked distance on the wrist, backed by WatchDashboardModel which reads
// through MilewayWatchGraph (P4.3) into the `:sharedWatch` KMP framework's WatchDomainFacade.

import SwiftUI

@main
struct MilewayWatchApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
