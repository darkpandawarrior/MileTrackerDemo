package com.mileway.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.Text
import com.mileway.wear.theme.WearMilewayTheme

/**
 * P2.1: the Wear app's single entry point (mirrors [com.mileway.LauncherActivity]'s role on the
 * phone, minus its login/PIN theatre — the watch renders whatever [WatchFacade] reports for the
 * signed-in phone session, it never authenticates on its own). Boots [WearAppGraph] once, then hosts
 * [WearRoot]. Real dashboard/trip-list/detail content lands in P2.4/P2.5; this task only needs a
 * placeholder so `:wear:assembleNoGmsDebug`/`assembleGmsDebug` produce a launchable app.
 */
class WearActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WearAppGraph.start(this)
        setContent { WearRoot() }
    }
}

/** Placeholder root composable — replaced by the real dashboard/trip-list/detail flow in P2.4/P2.5. */
@Composable
fun WearRoot() {
    WearMilewayTheme {
        Text(
            text = "Mileway",
            modifier = Modifier.fillMaxSize(),
        )
    }
}
