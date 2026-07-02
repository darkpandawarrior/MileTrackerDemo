package com.mileway.platform.gms

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.mileway.core.data.watch.TrackingCommand
import com.mileway.core.data.watch.TrackingCommandCodec
import com.mileway.feature.tracking.manager.TrackingController
import io.github.aakira.napier.Napier
import org.koin.mp.KoinPlatform

private const val TAG = "WearTrackingCommandService"

/** Same DataLayer message path `wear/src/gms`'s `WearTrackingCommandSender` (P2.10) sends on. */
internal const val TRACK_COMMAND_PATH = "/mileway/track-command"

/**
 * P2.10: the phone-side (`app/src/gms`) receiver for the watch->phone start/stop-tracking
 * command — the reverse direction of P2.9's phone->watch snapshot sync. System-instantiated (via
 * the `WearableListenerService` manifest registration, mirroring
 * [MilewayFirebaseMessagingService]'s `KoinPlatform.getKoin()` lookup pattern since Koin cannot
 * constructor-inject a framework-instantiated service), so it resolves [TrackingController] from
 * the already-running app process's Koin graph rather than building its own.
 *
 * gms flavor ONLY — confined to `app/src/gms` per the flavor-isolation gotcha in PLAN_V23 §7;
 * `noGms`/`main` never reference this class or `com.google.android.gms.wearable.*`.
 */
class WearTrackingCommandService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != TRACK_COMMAND_PATH) return
        val command = TrackingCommandCodec.decode(messageEvent.data)
        if (command == null) {
            Napier.e(tag = TAG, message = "malformed track-command payload, ignoring")
            return
        }
        val trackingController =
            runCatching { KoinPlatform.getKoin().getOrNull<TrackingController>() }.getOrNull()
        if (trackingController == null) {
            Napier.e(tag = TAG, message = "TrackingController unavailable, dropping command")
            return
        }
        when (command.action) {
            TrackingCommand.Action.START -> trackingController.start(command.token)
            TrackingCommand.Action.STOP -> trackingController.stop(command.token)
        }
    }
}
