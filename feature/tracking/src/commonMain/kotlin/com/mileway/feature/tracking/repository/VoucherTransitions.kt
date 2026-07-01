package com.mileway.feature.tracking.repository

import com.mileway.core.data.model.db.VoucherStatus

/**
 * P3.2: the legal voucher status-lifecycle graph. This is Mileway's own simplified state
 * machine — not a port of the reference app's string-based status normalizer — expressed as a
 * pure `from -> allowed-next` lookup so both [VoucherRepository] and any test can reason about
 * it without touching the database.
 *
 * Graph: `DRAFT -> PENDING -> {APPROVED, REJECTED, SETTLED}`. [VoucherStatus.APPROVED],
 * [VoucherStatus.REJECTED] and [VoucherStatus.SETTLED] are terminal (no outbound edges) — once a
 * voucher is decided or paid out, it does not move again.
 */
object VoucherTransitions {
    private val graph: Map<VoucherStatus, Set<VoucherStatus>> =
        mapOf(
            VoucherStatus.DRAFT to setOf(VoucherStatus.PENDING),
            VoucherStatus.PENDING to setOf(VoucherStatus.APPROVED, VoucherStatus.REJECTED, VoucherStatus.SETTLED),
            VoucherStatus.APPROVED to emptySet(),
            VoucherStatus.REJECTED to emptySet(),
            VoucherStatus.SETTLED to emptySet(),
        )

    /** The set of statuses [from] may legally move to next. Empty for terminal statuses. */
    fun allowed(from: VoucherStatus): Set<VoucherStatus> = graph.getValue(from)

    /** Whether moving from [from] directly to [to] is a legal single-step transition. */
    fun isAllowed(
        from: VoucherStatus,
        to: VoucherStatus,
    ): Boolean = to in allowed(from)
}
