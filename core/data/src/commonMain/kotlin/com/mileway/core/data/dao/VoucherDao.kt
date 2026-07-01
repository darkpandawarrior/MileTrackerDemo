package com.mileway.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mileway.core.data.model.db.VoucherEntity
import kotlinx.coroutines.flow.Flow

/** P3.1: shared voucher store — the single source both Create Voucher and Voucher History bind to. */
@Dao
interface VoucherDao {
    @Query("SELECT * FROM vouchers ORDER BY createdAtMs DESC")
    fun observeAll(): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM vouchers ORDER BY createdAtMs DESC")
    suspend fun getAll(): List<VoucherEntity>

    @Query("SELECT COUNT(*) FROM vouchers")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voucher: VoucherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vouchers: List<VoucherEntity>)

    /** P3.2: the single column a status-lifecycle transition mutates. */
    @Query("UPDATE vouchers SET status = :status WHERE voucherNumber = :voucherNumber")
    suspend fun updateStatus(
        voucherNumber: String,
        status: String,
    )

    @Query("SELECT * FROM vouchers WHERE voucherNumber = :voucherNumber LIMIT 1")
    suspend fun getByNumber(voucherNumber: String): VoucherEntity?
}
