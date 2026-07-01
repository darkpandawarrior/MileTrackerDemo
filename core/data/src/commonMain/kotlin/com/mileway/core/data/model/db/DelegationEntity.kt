package com.mileway.core.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PLAN_V22 P6.3: a single approval-delegation grant — "I authorise [delegateName] to act for me
 * within [scope] until [expiresAtMillis]." This is the approval-delegation concept
 * (`DelegationScreen`'s "My Delegations" list), distinct from the account-switch/session-level
 * "act on behalf of another persona" concept (`isActingAsDelegate`, see PLAN_V22 §2's Architecture
 * note) — the two are never merged.
 *
 * [isActive] is the caller-facing on/off toggle (a delegation can be paused without being
 * revoked/deleted); revoking removes the row outright.
 */
@Entity(tableName = "delegations")
data class DelegationEntity(
    @PrimaryKey
    val id: String,
    val delegateName: String,
    val scope: String,
    val expiresAtMillis: Long,
    val isActive: Boolean,
    val createdAt: Long,
)
