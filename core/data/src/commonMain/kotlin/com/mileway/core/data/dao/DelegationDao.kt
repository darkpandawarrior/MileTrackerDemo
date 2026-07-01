package com.mileway.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mileway.core.data.model.db.DelegationEntity
import kotlinx.coroutines.flow.Flow

/** PLAN_V22 P6.3: the persisted approval-delegation store — see [DelegationEntity]. */
@Dao
interface DelegationDao {
    @Query("SELECT * FROM delegations ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<DelegationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DelegationEntity)

    @Query("DELETE FROM delegations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE delegations SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(
        id: String,
        isActive: Boolean,
    )
}
