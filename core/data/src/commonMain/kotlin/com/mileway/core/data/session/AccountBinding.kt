package com.mileway.core.data.session

/**
 * The ownership pointer a persisted trip/session was stamped with at start time — mirrors
 * `SavedTrack`'s `started_by_*` columns (see [SavedTrack][com.mileway.core.data.model.db.SavedTrack])
 * and [CurrentTrackData][com.mileway.core.data.model.db.CurrentTrackData]'s equivalent fields, kept
 * as its own small value type here so the matching logic below doesn't need to depend on either
 * Room or DataStore models directly.
 */
data class TripOwnershipBinding(
    val accountId: String?,
    val employeeCode: String,
    val accountEmail: String,
    val tenant: String,
)

/**
 * The currently-signed-in identity to compare a [TripOwnershipBinding] against — sourced from
 * [ActiveAccountSource] (accountId) and [SessionState] (employeeCode/email/tenant, PLAN_V22 P3.1).
 */
data class SignedInIdentity(
    val accountId: String?,
    val employeeCode: String?,
    val accountEmail: String?,
    val tenant: String,
)

/**
 * PLAN_V22 P3.3: pure, unit-testable boolean-mismatch session/account-ownership check. A binding
 * "belongs to" the current identity when its non-blank pointer fields all agree with the
 * signed-in identity; any populated field that disagrees marks it as a different account's
 * session. Blank/null fields on either side are treated as "unknown" rather than a mismatch, since
 * older or partially-stamped records (pre-P3.3 data, or a guest session with no email) shouldn't be
 * treated as belonging to a stranger just because one field was never captured.
 */
fun doesSessionBelongTo(
    binding: TripOwnershipBinding,
    currentIdentity: SignedInIdentity,
): Boolean {
    fun matches(
        bindingValue: String?,
        identityValue: String?,
    ): Boolean {
        if (bindingValue.isNullOrBlank() || identityValue.isNullOrBlank()) return true
        return bindingValue == identityValue
    }

    return matches(binding.accountId, currentIdentity.accountId) &&
        matches(binding.employeeCode, currentIdentity.employeeCode) &&
        matches(binding.accountEmail, currentIdentity.accountEmail) &&
        matches(binding.tenant, currentIdentity.tenant)
}

/** Convenience inverse of [doesSessionBelongTo], read more naturally at guard call sites. */
fun isSessionFromDifferentAccount(
    binding: TripOwnershipBinding,
    currentIdentity: SignedInIdentity,
): Boolean = !doesSessionBelongTo(binding, currentIdentity)
