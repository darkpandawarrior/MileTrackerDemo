package com.mileway.feature.profile.model

/**
 * PLAN_V22 P6.8: lifecycle of a submitted [SupportTicket] — a fresh submission starts [OPEN]; the
 * demo has no backend to progress it further, so [IN_PROGRESS]/[RESOLVED] exist only as the status
 * vocabulary "My Tickets" renders (matching a real support-ticket lifecycle shape), not something
 * this offline app can transition automatically.
 */
enum class SupportTicketStatus { OPEN, IN_PROGRESS, RESOLVED }

/**
 * A single Help & Support ticket submitted via `HelpScreen`'s "Contact Support" form, listed on
 * "My Tickets" — real, persisted replacement for the form's previous fire-and-forget snackbar with
 * nothing inspectable afterward.
 */
data class SupportTicket(
    val id: String,
    val subject: String,
    val body: String,
    val createdAtMs: Long,
    val status: SupportTicketStatus,
)
