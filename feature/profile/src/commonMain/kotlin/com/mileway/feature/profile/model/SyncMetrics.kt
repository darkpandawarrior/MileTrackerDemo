package com.mileway.feature.profile.model

/**
 * PLAN_V22 P6.7: local, mock-data shape for Settings' "Sync Diagnostics" card — the same kind of
 * field shape a real sync-status surface would expose, but sourced from an in-memory counter (see
 * `SyncDiagnosticsRepository`) rather than any network call, since Mileway has no backend to sync
 * against yet.
 */
data class SyncMetrics(
    val locationsSynced: Int = 0,
    val eventsSynced: Int = 0,
    val failedAttempts: Int = 0,
    val lastSyncTimeMs: Long? = null,
    val isSyncing: Boolean = false,
)
