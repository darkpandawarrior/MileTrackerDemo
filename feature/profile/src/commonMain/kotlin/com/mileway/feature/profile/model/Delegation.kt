package com.mileway.feature.profile.model

/**
 * PLAN_V22 P6.3: a single approval-delegation grant, as rendered by `DelegationScreen`'s
 * "My Delegations" list. This is the approval-delegation concept ("delegate your approval
 * authority to a teammate"), not the account-switch/session-delegate concept — the two are kept
 * separately modeled per PLAN_V22 §2's Architecture note.
 */
data class Delegation(
    val id: String,
    val delegateName: String,
    val scope: String,
    val expiresAtMillis: Long,
    val isActive: Boolean,
)
