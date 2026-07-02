// P6.3/P6.4: extension entry point — registers home + Lock Screen accessory widgets and the
// P6.4 tracking Live Activity.

import WidgetKit
import SwiftUI

@main
struct MilewayWidgetsBundle: WidgetBundle {
    var body: some Widget {
        MileageHomeWidget()
        MileageLockScreenWidget()
        if #available(iOS 16.2, *) {
            TrackingLiveActivity()
        }
    }
}
