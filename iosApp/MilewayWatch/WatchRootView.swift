// P4.4: the watch app's root — owns the single WatchDashboardModel instance and the
// NavigationStack; replaces the P4.2 stub ContentView.

import SwiftUI

struct WatchRootView: View {
    @StateObject private var model = WatchDashboardModel()

    var body: some View {
        NavigationStack {
            WatchDashboardView(model: model)
        }
    }
}

#Preview {
    WatchRootView()
}
