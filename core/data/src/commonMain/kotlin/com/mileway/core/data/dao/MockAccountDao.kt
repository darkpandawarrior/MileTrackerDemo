package com.mileway.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mileway.core.data.model.db.MockAccountEntity
import kotlinx.coroutines.flow.Flow

/** P1.1: the persisted multi-persona account store — see [MockAccountEntity]. */
@Dao
interface MockAccountDao {
    @Query("SELECT * FROM mock_accounts ORDER BY createdAtMs ASC")
    fun observeAll(): Flow<List<MockAccountEntity>>

    @Query("SELECT COUNT(*) FROM mock_accounts")
    suspend fun count(): Int

    @Query("SELECT * FROM mock_accounts WHERE accountId = :accountId LIMIT 1")
    suspend fun getById(accountId: String): MockAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: MockAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(accounts: List<MockAccountEntity>)

    @Query("DELETE FROM mock_accounts WHERE accountId = :accountId")
    suspend fun delete(accountId: String)

    @Query("UPDATE mock_accounts SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE mock_accounts SET isActive = 1 WHERE accountId = :accountId")
    suspend fun markActive(accountId: String)

    /** Clears every row's [MockAccountEntity.isActive] then sets it on exactly [accountId], atomically. */
    @Transaction
    suspend fun setActive(accountId: String) {
        clearActive()
        markActive(accountId)
    }
}
