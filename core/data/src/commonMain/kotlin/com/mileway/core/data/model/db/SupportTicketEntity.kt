package com.mileway.core.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PLAN_V22 P6.8: a single Help & Support ticket — real, persisted replacement for `HelpScreen`'s
 * "Contact Support" button, which previously only surfaced a fire-and-forget snackbar with nothing
 * inspectable afterward. [status] stores [com.mileway.feature.profile.data.SupportTicketStatus]'s
 * enum name as `TEXT`, the same converter-free enum-as-string pattern already used for
 * `notifications.type`.
 */
@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey
    val id: String,
    val subject: String,
    val body: String,
    val createdAtMs: Long,
    val status: String,
)
