package com.mileway.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mileway.core.data.model.db.PassportDetailsEntity
import kotlinx.coroutines.flow.Flow

/** P6.2: the persisted passport-details singleton row — see [PassportDetailsEntity]. */
@Dao
interface PassportDetailsDao {
    @Query("SELECT * FROM passport_details WHERE id = :id LIMIT 1")
    fun observe(id: String = PassportDetailsEntity.SINGLETON_ID): Flow<PassportDetailsEntity?>

    @Query("SELECT * FROM passport_details WHERE id = :id LIMIT 1")
    suspend fun get(id: String = PassportDetailsEntity.SINGLETON_ID): PassportDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PassportDetailsEntity)
}
