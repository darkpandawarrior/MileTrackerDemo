package com.mileway.core.data.session

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * PLAN_V22 P3.2: pure-function coverage for [isSessionFresh], the 25-minute soft staleness check
 * behind the "Re-confirm it's you" sheet.
 */
class SessionFreshnessTest {
    private val threshold = DEFAULT_SESSION_FRESHNESS_THRESHOLD_MS

    @Test
    fun `session signed in just now is fresh`() {
        assertTrue(isSessionFresh(now = 1_000_000L, signedInAtMillis = 1_000_000L, thresholdMs = threshold))
    }

    @Test
    fun `session just under the threshold is fresh`() {
        val signedInAt = 0L
        val now = threshold - 1
        assertTrue(isSessionFresh(now, signedInAt, threshold))
    }

    @Test
    fun `session at or past the threshold is stale`() {
        val signedInAt = 0L
        assertFalse(isSessionFresh(now = threshold, signedInAtMillis = signedInAt, thresholdMs = threshold))
        assertFalse(isSessionFresh(now = threshold + 60_000L, signedInAtMillis = signedInAt, thresholdMs = threshold))
    }

    @Test
    fun `null signedInAtMillis is treated as fresh (nothing to re-confirm against)`() {
        assertTrue(isSessionFresh(now = 999_999_999L, signedInAtMillis = null))
    }

    @Test
    fun `default threshold is 25 minutes`() {
        assertTrue(isSessionFresh(now = 25 * 60 * 1000 - 1, signedInAtMillis = 0L))
        assertFalse(isSessionFresh(now = 25 * 60 * 1000, signedInAtMillis = 0L))
    }
}
