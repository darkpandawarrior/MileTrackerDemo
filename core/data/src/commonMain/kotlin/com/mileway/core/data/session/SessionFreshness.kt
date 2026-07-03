package com.mileway.core.data.session

/** Default staleness window: 25 minutes, mirroring the reference app's PIN/biometric re-confirm gate. */
const val DEFAULT_SESSION_FRESHNESS_THRESHOLD_MS: Long = 25 * 60 * 1000

/**
 * PLAN_V22 P3.2: pure, unit-testable staleness check modeling the reference app's PIN/biometric
 * "re-confirm it's you after N minutes idle" concept — no real biometric infra needed since
 * Mileway doesn't have one to gate on yet (that's P2.3's narrower scope, the actual switch-account
 * gate). This is UX-pattern parity only: a soft, dismissible nudge, not a hard block, since Mileway
 * has no real secret this would protect.
 *
 * A null [signedInAtMillis] (no session, or a pre-P3.1 session that never stamped one) is treated
 * as fresh rather than stale — there is nothing to re-confirm against, so nudging the user makes no
 * sense.
 */
fun isSessionFresh(
    now: Long,
    signedInAtMillis: Long?,
    thresholdMs: Long = DEFAULT_SESSION_FRESHNESS_THRESHOLD_MS,
): Boolean {
    if (signedInAtMillis == null) return true
    return now - signedInAtMillis < thresholdMs
}
