package com.mileway.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mileway.core.data.model.db.VehicleDetailsEntity
import kotlinx.coroutines.flow.Flow

/** P6.2: the persisted vehicle-details singleton row — see [VehicleDetailsEntity]. */
@Dao
interface VehicleDetailsDao {
    @Query("SELECT * FROM vehicle_details WHERE id = :id LIMIT 1")
    fun observe(id: String = VehicleDetailsEntity.SINGLETON_ID): Flow<VehicleDetailsEntity?>

    @Query("SELECT * FROM vehicle_details WHERE id = :id LIMIT 1")
    suspend fun get(id: String = VehicleDetailsEntity.SINGLETON_ID): VehicleDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VehicleDetailsEntity)
}
