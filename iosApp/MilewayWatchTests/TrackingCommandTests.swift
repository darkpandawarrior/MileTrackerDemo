// Z.5d acceptance: covers the watch->phone command codec `WatchTrackingCommandSender`/
// `PhoneWatchSyncBridge` round-trip through `WCSession.sendMessage`/`didReceiveMessage`
// ([String: Any]), mirroring `MilewaySyncModelsTests`'s coverage of the reverse-direction codec.

import XCTest
@testable import MilewayWatch

final class TrackingCommandTests: XCTestCase {
    func testRoundTripPreservesStartAction() {
        let command = TrackingCommand(action: .start)

        let message = TrackingCommandCodec.encode(command)
        let decoded = TrackingCommandCodec.decode(message)

        XCTAssertEqual(decoded, command)
    }

    func testRoundTripPreservesStopAction() {
        let command = TrackingCommand(action: .stop)

        let message = TrackingCommandCodec.encode(command)
        let decoded = TrackingCommandCodec.decode(message)

        XCTAssertEqual(decoded, command)
    }

    func testDecodeReturnsNilForMalformedMessage() {
        XCTAssertNil(TrackingCommandCodec.decode(["action": "pause"]))
    }

    func testDecodeReturnsNilForEmptyMessage() {
        XCTAssertNil(TrackingCommandCodec.decode([:]))
    }
}
