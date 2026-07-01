package com.mileway.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mileway.core.data.model.db.SessionEntity
import kotlinx.coroutines.flow.Flow

/** PLAN_V22 P6.4: the persisted active-sessions store — see [SessionEntity]. */
@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY lastActiveMillis DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sessions: List<SessionEntity>)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun delete(id: String)

    /** Removes every row except the current device's — the bulk "Sign out all other sessions" action. */
    @Query("DELETE FROM sessions WHERE isCurrent = 0")
    suspend fun deleteAllExceptCurrent()
}
