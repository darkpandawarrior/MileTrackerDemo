package com.mileway.feature.profile.model

enum class AdvanceStatus { PENDING, UNDER_REVIEW, APPROVED, DISBURSED, REJECTED }

enum class AdvanceMode { CASH, CARD_LINKED }

data class AdvanceRecord(
    val id: String,
    val amountRupees: Double,
    val purpose: String,
    val status: AdvanceStatus,
    val requestedDateMs: Long,
    val requiredByDate: String,
    val approverChain: List<ApproverStep> = emptyList(),
    val timeline: List<TimelineEntry> = emptyList(),
)

enum class ApproverStepStatus { PENDING, APPROVED, REJECTED }

data class ApproverStep(
    val name: String,
    val stageLabel: String,
    val status: ApproverStepStatus,
)

data class TimelineEntry(
    val label: String,
    val dateMs: Long,
)

enum class CardType { VISA, MASTERCARD }

enum class CardStatus { ACTIVE, BLOCKED }

data class CorporateCard(
    val id: String,
    val lastFourDigits: String,
    val cardType: CardType,
    val holderName: String,
    val balanceRupees: Double,
    val status: CardStatus,
    val expiryDate: String,
    val creditLimitRupees: Double,
)

data class CardTransaction(
    val id: String,
    val cardId: String,
    val merchantName: String,
    val amountRupees: Double,
    val dateMs: Long,
    val category: String,
)
