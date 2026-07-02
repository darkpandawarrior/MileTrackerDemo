// P6.3: TimelineProvider reading the shared App-Group snapshot store (P6.1's SnapshotCacheStore,
// mirrored here in plain Swift since a widget-extension process cannot use NSUserDefaults.standard
// and does not link the KMP `Mileway` framework — see MilewaySyncModels.swift's own doc comment for
// why the wire model is a plain Codable struct, not framework-backed).

import WidgetKit

/// Reads the same App-Group-shared `UserDefaults` key `SnapshotCacheStore` (core:data, iOS actual)
/// writes to. Deliberately re-declared here rather than imported: the widget extension is a
/// separate target that does not link `Mileway.framework`, and the wire shape is a plain JSON blob
/// keyed by string, so decoding it needs no shared framework — only the same App Group ID + key.
enum MileageSnapshotStore {
    private static let suiteName = "group.com.mileway.shared"
    private static let payloadKey = "watch_sync_payload_json"

    static func read() -> MilewaySyncPayload {
        guard
            let defaults = UserDefaults(suiteName: suiteName),
            let raw = defaults.string(forKey: payloadKey),
            let data = raw.data(using: .utf8),
            let payload = try? JSONDecoder().decode(MilewaySyncPayload.self, from: data)
        else {
            return .empty
        }
        return payload
    }

    /// Writes back through the same App-Group key the host app's `SnapshotCacheStore` uses, so the
    /// interactive App-Intent button's flag flip is visible next time the host app reads the cache.
    static func write(_ payload: MilewaySyncPayload) {
        guard
            let defaults = UserDefaults(suiteName: suiteName),
            let data = try? JSONEncoder().encode(payload),
            let raw = String(data: data, encoding: .utf8)
        else {
            return
        }
        defaults.set(raw, forKey: payloadKey)
    }
}

struct MileageWidgetEntry: TimelineEntry {
    let date: Date
    let payload: MilewaySyncPayload
}

struct MileageWidgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> MileageWidgetEntry {
        MileageWidgetEntry(date: Date(), payload: .empty)
    }

    func getSnapshot(in context: Context, completion: @escaping (MileageWidgetEntry) -> Void) {
        completion(MileageWidgetEntry(date: Date(), payload: MileageSnapshotStore.read()))
    }

    /// A single-entry timeline with `.never` policy: the widget has no periodic refresh work of
    /// its own (offline mock data, P6.1) — it re-renders whenever the system reloads it (e.g. after
    /// the host app calls `WidgetCenter.shared.reloadAllTimelines()` on a snapshot write) rather
    /// than polling on a schedule.
    func getTimeline(in context: Context, completion: @escaping (Timeline<MileageWidgetEntry>) -> Void) {
        let entry = MileageWidgetEntry(date: Date(), payload: MileageSnapshotStore.read())
        completion(Timeline(entries: [entry], policy: .never))
    }
}
