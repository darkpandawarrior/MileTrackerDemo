package com.mileway.wear.gms

import com.mileway.core.data.watch.NoopWatchSyncBridge
import com.mileway.core.data.watch.WatchSyncBridge
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * P2.9: noGms (FOSS/F-Droid) flavor — no Data Layer transport exists, so [WatchSyncBridge] binds
 * to [NoopWatchSyncBridge]. Keeps `:wear`'s `noGms` classpath free of
 * `com.google.android.gms.wearable.*` (the FOSS-purity guard, P2.2, enforces this at the
 * dependency-graph level).
 */
fun watchSyncKoinModule(): Module =
    module {
        single<WatchSyncBridge> { NoopWatchSyncBridge() }
    }
