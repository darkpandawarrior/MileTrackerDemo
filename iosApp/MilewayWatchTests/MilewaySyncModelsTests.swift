// P4.5 acceptance: covers the Codable payload codec both WCSessionDelegate sides round-trip
// through `WCSession.updateApplicationContext`/`didReceiveApplicationContext` ([String: Any]).

import XCTest
@testable import MilewayWatch

final class MilewaySyncModelsTests: XCTestCase {
    func testRoundTripPreservesAllFields() {
        let payload = MilewaySyncPayload(
            todayKm: 12.5,
            weekKm: 48.0,
            tripCount: 6,
            isTracking: true,
            isPaused: false,
            weekGoalProgress: 0.75,
            lastTripLabel: "Commute",
            updatedAtMs: 1_700_000_000_000
        )

        let context = MilewaySyncCodec.encode(payload)
        let decoded = MilewaySyncCodec.decode(context)

        XCTAssertEqual(decoded, payload)
    }

    func testRoundTripPreservesNilLastTripLabel() {
        let payload = MilewaySyncPayload(lastTripLabel: nil)

        let context = MilewaySyncCodec.encode(payload)
        let decoded = MilewaySyncCodec.decode(context)

        XCTAssertEqual(decoded, payload)
        XCTAssertNil(decoded?.lastTripLabel)
    }

    func testDecodeReturnsNilForMalformedContext() {
        let malformed: [String: Any] = ["todayKm": "not-a-number"]

        XCTAssertNil(MilewaySyncCodec.decode(malformed))
    }

    func testDecodeReturnsNilForEmptyContext() {
        XCTAssertNil(MilewaySyncCodec.decode([:]))
    }
}
