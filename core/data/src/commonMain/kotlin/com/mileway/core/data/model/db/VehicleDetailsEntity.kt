package com.mileway.core.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * P6.2: the vehicle linked to a profile. Single-row table by design (Mileway is a single-profile
 * demo per persona, mirroring [DraftExpenseEntity]'s singleton pattern) — a fixed [id] keeps
 * `upsert` idempotent without a lookup query first.
 */
@Entity(tableName = "vehicle_details")
data class VehicleDetailsEntity(
    @PrimaryKey
    val id: String = SINGLETON_ID,
    val make: String,
    val model: String,
    val registrationNumber: String,
    val fuelType: String,
    val seatingCapacity: Int,
    val updatedAtMs: Long,
) {
    companion object {
        const val SINGLETON_ID: String = "vehicle_details_singleton"
    }
}
