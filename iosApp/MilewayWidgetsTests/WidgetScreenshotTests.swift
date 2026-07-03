// showcase: renders the iOS WidgetKit widget views to PNGs via ImageRenderer — fixed-layout
// widget views render cleanly (no ScrollView), so no home-screen placement is needed. Writes to
// the host repo's docs/screenshots/. Run: xcodebuild test -scheme iosApp -sdk iphonesimulator
//      -only-testing:MilewayWidgetsTests/WidgetScreenshotTests

import SwiftUI
import WidgetKit
import XCTest
@testable import MilewayWidgets

final class WidgetScreenshotTests: XCTestCase {
    private let outDir = "/Users/darkpandawarrior/Repos/Mileway/docs/screenshots"

    private var mockEntry: MileageWidgetEntry {
        MileageWidgetEntry(
            date: Date(timeIntervalSince1970: 1_700_000_000),
            payload: MilewaySyncPayload(
                todayKm: 12.4,
                weekKm: 58.7,
                tripCount: 4,
                isTracking: true,
                weekGoalProgress: 0.587,
                lastTripLabel: "Commute"
            )
        )
    }

    @MainActor
    func testCaptureHomeWidget() throws {
        let view = MileageHomeWidgetView(entry: mockEntry)
            .frame(width: 329, height: 155)
        try render(view, to: "widget_ios_home.png")
    }

    @MainActor
    func testCaptureLockScreenWidget() throws {
        let view = MileageAccessoryRectangularView(entry: mockEntry)
            .frame(width: 160, height: 72)
        try render(view, to: "widget_ios_lockscreen.png")
    }

    @MainActor
    private func render(_ view: some View, to name: String) throws {
        let renderer = ImageRenderer(content: view)
        renderer.scale = 3
        guard let image = renderer.uiImage, let png = image.pngData() else {
            throw XCTSkip("ImageRenderer produced no image in this environment")
        }
        try FileManager.default.createDirectory(atPath: outDir, withIntermediateDirectories: true)
        let url = URL(fileURLWithPath: outDir).appendingPathComponent(name)
        try png.write(to: url)
        let attachment = XCTAttachment(data: png, uniformTypeIdentifier: "public.png")
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
