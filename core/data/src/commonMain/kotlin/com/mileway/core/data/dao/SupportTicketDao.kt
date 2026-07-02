package com.mileway.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mileway.core.data.model.db.SupportTicketEntity
import kotlinx.coroutines.flow.Flow

/** PLAN_V22 P6.8: the persisted "My Tickets" store — see [SupportTicketEntity]. */
@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAtMs DESC")
    fun observeAll(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SupportTicketEntity)
}
