package com.mileway.feature.profile.model

/**
 * PLAN_V22 P6.7: one row of Settings' "Permission Health" section. [isGranted] must be sourced
 * from a real platform check (`ContextCompat.checkSelfPermission` on Android — see
 * `PermissionHealthState.kt` in `androidMain`) rather than a hardcoded demo value.
 */
data class PermissionHealthEntry(
    val name: String,
    val isRequired: Boolean,
    val isGranted: Boolean,
)

/**
 * Derived summary of a [PermissionHealthEntry] list — the health ring's percentage plus the
 * "Required N/M" / "Recommended N/M" chip counts. Pure function of the input list so it's testable
 * without any Android framework dependency (no Robolectric needed).
 */
data class PermissionHealthSummary(
    val healthScorePercent: Int,
    val requiredGranted: Int,
    val requiredTotal: Int,
    val recommendedGranted: Int,
    val recommendedTotal: Int,
)

/**
 * Computes [PermissionHealthSummary] from the real granted/required ratio across all entries
 * (required and recommended alike), replacing the previous hardcoded "90%". An empty list scores
 * 100% (nothing required, nothing missing) rather than dividing by zero.
 */
fun computePermissionHealth(entries: List<PermissionHealthEntry>): PermissionHealthSummary {
    val required = entries.filter { it.isRequired }
    val recommended = entries.filterNot { it.isRequired }
    val grantedCount = entries.count { it.isGranted }
    val scorePercent =
        if (entries.isEmpty()) {
            100
        } else {
            (grantedCount * 100) / entries.size
        }
    return PermissionHealthSummary(
        healthScorePercent = scorePercent,
        requiredGranted = required.count { it.isGranted },
        requiredTotal = required.size,
        recommendedGranted = recommended.count { it.isGranted },
        recommendedTotal = recommended.size,
    )
}
