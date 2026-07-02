// P6.3: extension entry point — registers both widget kinds (home + Lock Screen accessory).

import WidgetKit
import SwiftUI

@main
struct MilewayWidgetsBundle: WidgetBundle {
    var body: some Widget {
        MileageHomeWidget()
        MileageLockScreenWidget()
    }
}
