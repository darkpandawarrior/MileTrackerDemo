package com.mileway.core.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * P3.1: the single shared voucher store both `CreateVoucherScreen` (feature/tracking) and
 * `VoucherHistoryScreen` (feature/logging) read/write through, replacing two disconnected
 * in-memory stores (`feature.tracking.repository.VoucherRecord` and a hardcoded
 * `feature.logging.repository.VoucherHistoryRepository` spec) that made a submitted voucher
 * invisible to history. [expenseRouteIdsJson] carries the linked trip/expense tokens as a
 * JSON-encoded `List<String>` (plain `TEXT` column, no `@TypeConverter`, mirroring the
 * `xJson: String` convention already used by [LogMilesDraftEntity]/[SubmitDraftEntity]) — this
 * also resolves the "creation vs history" id-shape mismatch (gap 17.7) since both sides now read
 * the same list instead of `expenseRouteIds: List<String>` vs a single `expenseId: String`.
 */
@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey
    val voucherNumber: String,
    val title: String,
    val category: String,
    val totalAmount: Double,
    val notes: String,
    val expenseRouteIdsJson: String,
    val status: String,
    val createdAtMs: Long,
) {
    companion object {
        private val stringListSerializer = ListSerializer(String.serializer())

        /** Encodes a list of expense/trip route ids into [expenseRouteIdsJson]'s stored form. */
        fun encodeExpenseRouteIds(ids: List<String>): String = Json.encodeToString(stringListSerializer, ids)

        /** Decodes [expenseRouteIdsJson] back into the list of expense/trip route ids. */
        fun decodeExpenseRouteIds(json: String): List<String> = runCatching { Json.decodeFromString(stringListSerializer, json) }.getOrDefault(emptyList())
    }
}
