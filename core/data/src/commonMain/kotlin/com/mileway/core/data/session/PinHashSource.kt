package com.mileway.core.data.session

/**
 * PLAN_V22 P7.4: the key [PinHashSource] is queried under for the session-level login PIN gate
 * (`SetPinScreen`/`CheckPinScreen`), as distinct from P2.3's per-persona switch-account PINs
 * (keyed by the real `accountId`). One login PIN gates the whole session regardless of which
 * persona is active, so it uses a fixed, non-persona key into the same store.
 */
const val PIN_GATE_ACCOUNT_ID: String = "session-login-pin"

/**
 * PLAN_V22 P2.3: per-demo-account PIN hash storage backing [SwitchAccountViewModel]'s
 * PIN-before-switch gate. Mirrors [ActiveAccountSource]'s interface-plus-platform-impl split so
 * `feature:profile` can unit test the gate against a fake instead of a real DataStore-backed
 * Context. Only the SHA-256 hex digest ([sha256Hex]) is ever persisted, never the raw PIN.
 *
 * PLAN_V22 P7.4 reuses this same store for the session-level login PIN gate, keyed by
 * [PIN_GATE_ACCOUNT_ID] instead of a real account id — same shape (hash-in, hash-out), no reason
 * to duplicate the interface or its platform DataStore-backed implementation for a second PIN.
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
