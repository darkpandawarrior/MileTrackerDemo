package com.mileway.core.data.session

/**
 * PLAN_V22 P2.3: per-demo-account PIN hash storage backing [SwitchAccountViewModel]'s
 * PIN-before-switch gate. Mirrors [ActiveAccountSource]'s interface-plus-platform-impl split so
 * `feature:profile` can unit test the gate against a fake instead of a real DataStore-backed
 * Context. Only the SHA-256 hex digest ([sha256Hex]) is ever persisted, never the raw PIN.
 */
interface PinHashSource {
    /** The stored PIN hash for [accountId], or `null` if no PIN has ever been set for it. */
    suspend fun getPinHash(accountId: String): String?

    /** Persists [pinHash] (already hashed by the caller) as [accountId]'s PIN. */
    suspend fun setPinHash(
        accountId: String,
        pinHash: String,
    )
}
