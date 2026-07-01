package com.mileway.core.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * P6.2: the passport linked to a profile. Single-row table by design, same pattern as
 * [VehicleDetailsEntity] / [DraftExpenseEntity].
 */
@Entity(tableName = "passport_details")
data class PassportDetailsEntity(
    @PrimaryKey
    val id: String = SINGLETON_ID,
    val passportNumber: String,
    val issuingCountry: String,
    val expiryDateMillis: Long,
    val updatedAtMs: Long,
) {
    companion object {
        const val SINGLETON_ID: String = "passport_details_singleton"
    }
}
