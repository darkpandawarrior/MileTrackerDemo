package com.mileway.core.data.session

import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for "which persona is active" (PLAN_V22 P2.1). Platform
 * implementations ([ActiveAccountStore] on Android/iOS) persist this to DataStore so it survives
 * process death; `commonMain` only declares the read/write contract, mirroring
 * [CurrentTrackDataSource]'s interface-plus-platform-impl split so `ProfileViewModel` can be unit
 * tested against a fake instead of a real DataStore-backed Context.
 */
interface ActiveAccountSource {
    val activeAccountId: Flow<String?>

    /** Persist which persona is now active. */
    suspend fun setActiveAccountId(accountId: String)
}
