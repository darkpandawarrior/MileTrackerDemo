package com.mileway.feature.tracking.repository

import com.mileway.core.data.dao.VoucherDao
import com.mileway.core.data.model.db.VoucherEntity

data class VoucherRecord(
    val voucherNumber: String,
    val title: String,
    val category: String,
    val totalAmount: Double,
    val notes: String,
    val expenseRouteIds: List<String>,
    val createdAtMs: Long,
    val status: String = "Draft",
)

/**
 * P3.1: delegates to the shared [VoucherDao] (`core/data`) instead of a private in-memory list,
 * so a voucher created here is immediately visible to `feature/logging`'s
 * `VoucherHistoryRepository` — the two used to be entirely disconnected stores.
 */
class VoucherRepository(private val dao: VoucherDao) {
    suspend fun save(record: VoucherRecord) {
        dao.insert(
            VoucherEntity(
                voucherNumber = record.voucherNumber,
                title = record.title,
                category = record.category,
                totalAmount = record.totalAmount,
                notes = record.notes,
                expenseRouteIdsJson = VoucherEntity.encodeExpenseRouteIds(record.expenseRouteIds),
                status = record.status,
                createdAtMs = record.createdAtMs,
            ),
        )
    }

    suspend fun getAll(): List<VoucherRecord> = dao.getAll().map { it.toRecord() }
}

private fun VoucherEntity.toRecord() =
    VoucherRecord(
        voucherNumber = voucherNumber,
        title = title,
        category = category,
        totalAmount = totalAmount,
        notes = notes,
        expenseRouteIds = VoucherEntity.decodeExpenseRouteIds(expenseRouteIdsJson),
        createdAtMs = createdAtMs,
        status = status,
    )
