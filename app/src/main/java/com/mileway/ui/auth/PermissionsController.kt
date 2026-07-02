package com.mileway.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * PLAN_V22 P7.5: thin wrapper around the real
 * [androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions] launcher
 * for the two runtime permissions [WelcomeDisclaimerSheet] discloses before the first trip:
 * `ACCESS_FINE_LOCATION` (to record the trip route) and, on API 33+, `POST_NOTIFICATIONS` (for the
 * tracking foreground-service status notification — a no-op request below API 33, where the
 * permission doesn't exist).
 *
 * Both permissions are already declared in `AndroidManifest.xml`; this controller only drives the
 * runtime request flow, it does not change what the manifest grants at install time.
 *
 * @param onResult invoked once with the granted/denied map for every permission actually
 *   requested, after the system dialog is dismissed either way.
 */
class PermissionsController internal constructor(
    private val launch: () -> Unit,
) {
    /** Launches the real system permission dialog for the requested permissions. */
    fun request() = launch()
}

/** The runtime permissions [WelcomeDisclaimerSheet] requests, gated by API level. */
internal fun requestedDisclaimerPermissions(): Array<String> =
    buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

@Composable
fun rememberPermissionsController(onResult: (Map<String, Boolean>) -> Unit): PermissionsController {
    val permissions = remember { requestedDisclaimerPermissions() }
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { grants -> onResult(grants) }

    return remember(launcher, permissions) {
        PermissionsController(launch = { launcher.launch(permissions) })
    }
}

/** True if `ACCESS_FINE_LOCATION` is currently granted for this app install. */
@Composable
fun rememberHasLocationPermission(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * True if `POST_NOTIFICATIONS` is currently granted for this app install. Always true below API
 * 33, where the permission doesn't exist and notifications are granted implicitly.
 */
@Composable
fun rememberHasNotificationPermission(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
