package com.mileway.core.data.watch

import platform.Foundation.NSUserDefaults

/**
 * iOS actual for [SnapshotCache] (P6.1). Unlike an Android app widget, a `WidgetKit` extension is
 * a genuinely separate process/sandbox from the host app, so a plain `NSUserDefaults.standardUserDefaults`
 * (app-sandboxed) is NOT readable by the extension — only an **App Group** shared container is,
 * via `NSUserDefaults(suiteName:)` pointed at the group container ID both targets declare in their
 * entitlements.
 *
 * [APP_GROUP_ID] is the placeholder both the host app target and the (P6.3) `MilewayWidgets`
 * extension target must add to their `com.apple.security.application-groups` entitlement in
 * `project.yml`; wiring that Xcode-side entitlement is explicitly P6.3's job, not this task's.
 * Without that entitlement (e.g. running before P6.3 exists), `NSUserDefaults(suiteName:)` still
 * hands back a working (app-sandboxed, not group-shared) defaults instance on-device, so the phone
 * app's own read/write path works standalone today and starts being extension-visible the moment
 * P6.3 adds the entitlement.
 */
class SnapshotCacheStore : SnapshotCache {
    private val defaults: NSUserDefaults = NSUserDefaults(suiteName = APP_GROUP_ID)

    override suspend fun write(payload: WatchSyncPayload) {
        defaults.setObject(SnapshotCacheCodec.encode(payload), PAYLOAD_KEY)
        defaults.synchronize()
    }

    override suspend fun read(): WatchSyncPayload? = SnapshotCacheCodec.decode(defaults.stringForKey(PAYLOAD_KEY))

    private companion object {
        const val APP_GROUP_ID = "group.com.mileway.shared"
        const val PAYLOAD_KEY = "watch_sync_payload_json"
    }
}
