package com.mileway.wear

import android.content.Context
import com.mileway.core.data.di.coreDataModule
import com.mileway.core.platform.di.platformModule
import com.mileway.feature.tracking.di.trackingModule
import com.mileway.stub.di.stubModule
import com.mileway.wear.gms.watchSyncKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 * P2.4: `:wear`'s own module for its Compose screens' ViewModels — kept separate from
 * `trackingModule` (which is shared with the phone/iOS graph and knows nothing about Wear-specific
 * presentation types like [com.mileway.wear.WearViewModel]).
 */
private val wearModule =
    module {
        viewModelOf(::WearViewModel)
    }

/**
 * P2.1: the Wear app's own Koin bootstrap.
 *
 * Deliberately does NOT reuse `core:ui`'s `initKoin()` — that helper exists to prepend
 * `platformModule()` for the CMP phone/iOS graph, but `core:ui` itself is the Compose Multiplatform
 * theming module (`MilewayTheme`, the phone Material3 design system) that Wear OS must never depend
 * on (Wear renders with `androidx.wear.compose`, its own theme — see P2.3). [WearAppGraph] wires the
 * same headless graph the phone boots (`platformModule` + `coreDataModule` + `stubModule` +
 * `trackingModule`, all Android-actual/mock, zero network) without pulling in a single Compose
 * Multiplatform dependency, so [WearActivity], the tile and the complication services (P2.6/P2.7)
 * can all resolve `WatchFacade` and friends from the one shared instance.
 *
 * P2.9: also wires [watchSyncKoinModule] — the per-flavor `WatchSyncBridge` binding
 * (`wear/src/gms`'s real Data Layer bridge, `wear/src/noGms`'s [com.mileway.core.data.watch.NoopWatchSyncBridge]).
 */
object WearAppGraph {

    /** Idempotent: Activity, tile and complication processes may all call this before touching Koin. */
    fun start(context: Context) {
        val alreadyStarted = runCatching { KoinPlatform.getKoin() }.isSuccess
        if (alreadyStarted) stopKoin()
        startKoin {
            androidContext(context.applicationContext)
            androidLogger(Level.ERROR)
            modules(
                platformModule(),
                coreDataModule,
                stubModule,
                trackingModule,
                watchSyncKoinModule(),
                wearModule,
            )
        }
    }
}
