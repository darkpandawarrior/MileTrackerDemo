package com.mileway.core.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
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
 *
 * P3.4: [category] is now the typed, exhaustive [VoucherCategory] enum (not a free-text
 * `String`) — [VoucherCategoryConverters] round-trips it through the same `TEXT` column via its
 * [VoucherCategory.label], so the on-disk shape (and every existing demo row's stored value) is
 * unchanged.
 */
@Entity(tableName = "vouchers")
@TypeConverters(VoucherCategoryConverters::class)
data class VoucherEntity(
    @PrimaryKey
    val voucherNumber: String,
    val title: String,
    val category: VoucherCategory,
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

/**
 * P3.4: Room [TypeConverter]s that store [VoucherCategory] in the `vouchers.category` `TEXT`
 * column as its [VoucherCategory.label] — the same strings the column already held back when
 * `category` was a free-text `String` — so no data migration is needed for existing rows.
 * [fromLabel] falls back to [VoucherCategory.OTHER] for any unrecognized/legacy value rather than
 * throwing, mirroring the defensive `runCatching`/`getOrDefault` style already used by
 * [VoucherEntity.decodeExpenseRouteIds].
 */
object VoucherCategoryConverters {
    @TypeConverter
    fun toLabel(category: VoucherCategory): String = category.label

    @TypeConverter
    fun fromLabel(label: String): VoucherCategory = VoucherCategory.entries.firstOrNull { it.label == label } ?: VoucherCategory.OTHER
}
