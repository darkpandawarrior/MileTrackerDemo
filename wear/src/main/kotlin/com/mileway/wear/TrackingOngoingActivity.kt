package com.mileway.wear

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status

/**
 * P2.8: the watch-side "ongoing activity" surface (the Wear equivalent of the phone's foreground
 * tracking notification — see `LocationTrackingService.buildNotification` on `feature:tracking`).
 * [WearViewModel] drives [post]/[cancel] as a side effect of [com.mileway.feature.tracking.service.TrackingServiceApi.trackingState]
 * flipping live/idle (wired from [WearActivity]); this object stays a pure Android-notification
 * builder with no Koin/ViewModel dependency of its own, mirroring [MileageTileService]'s
 * cache-only-read split between "build the surface" and "decide when to show it".
 */
object TrackingOngoingActivity {

    private const val CHANNEL_ID = "mileway_tracking"
    private const val NOTIFICATION_ID = 1001

    /** Builds (and does NOT post) the ongoing-tracking notification for [distanceKm] tracked so far. */
    fun buildNotification(context: Context, distanceKm: Double): Notification {
        ensureChannel(context)

        val tapIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(Intent.ACTION_MAIN).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val status = Status.Builder()
            .addTemplate("${"%.2f".format(distanceKm)} km tracked")
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Tracking active")
            .setContentText("${"%.2f".format(distanceKm)} km")
            .setOngoing(true)
            .setContentIntent(tapIntent)

        OngoingActivity.Builder(context, NOTIFICATION_ID, builder)
            .setStaticIcon(android.R.drawable.ic_media_play)
            .setTouchIntent(tapIntent)
            .setStatus(status)
            .build()
            .apply(context)

        return builder.build()
    }

    /** Builds and posts the ongoing notification, called each time a live [distanceKm] update arrives. */
    fun post(context: Context, distanceKm: Double) {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, buildNotification(context, distanceKm))
    }

    /** Clears the ongoing notification — called once tracking stops (per P2.8's acceptance). */
    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Tracking", NotificationManager.IMPORTANCE_LOW),
            )
        }
    }
}
