package com.mileway.core.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PLAN_V22 P6.5: a single Notification Centre entry — real, persisted replacement for
 * `NotificationCentreScreen`'s previous `remember { mutableStateOf(NOTIFICATIONS) }` seed, which
 * reset on navigation away (and whose topbar subtitle hardcoded "174 unread" regardless of actual
 * state). [type] stores [com.mileway.feature.profile.data.NotifType]'s enum name as `TEXT`, the
 * same converter-free enum-as-string pattern already used for `vouchers.category`.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val body: String,
    val relativeTime: String,
    val isUnread: Boolean,
    val type: String,
    val createdAtMs: Long,
)
