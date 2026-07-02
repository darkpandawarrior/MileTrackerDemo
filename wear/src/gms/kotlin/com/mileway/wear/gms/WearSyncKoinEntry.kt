package com.mileway.wear.gms

import com.mileway.core.data.watch.WatchSyncBridge
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * P2.9: gms flavor — binds the real Data Layer [WatchSyncBridge]. Mirrors `:app`'s per-flavor
 * `platformServicesKoinModule()` entry-point pattern: one function of this name/shape per flavor
 * source set, called from [com.mileway.wear.WearAppGraph].
 */
fun watchSyncKoinModule(): Module =
    module {
        single<WatchSyncBridge> { WearDataLayerSyncBridge(androidContext()) }
    }
