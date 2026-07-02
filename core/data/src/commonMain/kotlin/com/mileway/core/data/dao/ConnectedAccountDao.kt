package com.mileway.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mileway.core.data.model.db.ConnectedAccountEntity
import kotlinx.coroutines.flow.Flow

/** PLAN_V22 P6.6: the persisted Connected Accounts list — see [ConnectedAccountEntity]. */
@Dao
interface ConnectedAccountDao {
    @Query("SELECT * FROM connected_accounts ORDER BY providerName ASC")
    fun observeAll(): Flow<List<ConnectedAccountEntity>>

    @Query("SELECT COUNT(*) FROM connected_accounts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ConnectedAccountEntity>)

    @Query("UPDATE connected_accounts SET isConnected = :isConnected, updatedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun setConnected(
        id: String,
        isConnected: Boolean,
        updatedAtMs: Long,
    )
}
