package com.mileway.core.data.session

import kotlinx.coroutines.flow.Flow

/**
 * `commonMain`-safe read contract over the signed-in [SessionState] (PLAN_V22 P3.3), mirroring
 * [ActiveAccountSource]'s interface-plus-platform-impl split. `SessionRepository` itself stays
 * androidMain/iosMain-only (it owns a platform DataStore file), but `commonMain` code that only
 * needs to *read* the current identity — e.g. [feature.tracking.viewmodel.TrackMilesViewModel]
 * stamping a new trip's `started_by_*` ownership pointer — can depend on this instead.
 */
interface SessionSource {
    val sessionState: Flow<SessionState>
}
