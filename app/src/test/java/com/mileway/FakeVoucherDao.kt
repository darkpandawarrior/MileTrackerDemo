package com.mileway

import com.mileway.core.data.dao.VoucherDao
import com.mileway.core.data.model.db.VoucherEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake for the shared [VoucherDao] (P3.1) — lets `CreateVoucherViewModelTest` and
 * `VoucherHistoryViewModelTest` construct their respective ViewModels against the *same* fake
 * store and assert cross-visibility, exactly like the real Room-backed DAO both feature modules
 * now bind to via Koin.
 */
class FakeVoucherDao : VoucherDao {
    private val vouchers = MutableStateFlow<Map<String, VoucherEntity>>(emptyMap())

    override fun observeAll(): Flow<List<VoucherEntity>> = vouchers.map { it.values.sortedByDescending { v -> v.createdAtMs } }

    override suspend fun getAll(): List<VoucherEntity> = vouchers.value.values.sortedByDescending { it.createdAtMs }

    override suspend fun count(): Int = vouchers.value.size

    override suspend fun insert(voucher: VoucherEntity) {
        vouchers.value = vouchers.value + (voucher.voucherNumber to voucher)
    }

    override suspend fun insertAll(vouchers: List<VoucherEntity>) {
        this.vouchers.value = this.vouchers.value + vouchers.associateBy { it.voucherNumber }
    }

    override suspend fun updateStatus(
        voucherNumber: String,
        status: String,
    ) {
        val existing = vouchers.value[voucherNumber] ?: return
        vouchers.value = vouchers.value + (voucherNumber to existing.copy(status = status))
    }

    override suspend fun getByNumber(voucherNumber: String): VoucherEntity? = vouchers.value[voucherNumber]
}
