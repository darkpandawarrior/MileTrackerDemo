package com.mileway.feature.profile.model

/**
 * P6.2: the vehicle linked to this profile — mirrors the reference app's vehicle field shape
 * (make/model, registration, fuel type, seating) at the data level; rendering is Mileway's own
 * tile + bottom sheet, never a port of the reference app's UI. Room-backed (single-row table,
 * same pattern as [com.mileway.core.data.model.db.DraftExpenseEntity]) so edits persist across
 * app restart. `null` from the repository means "not yet set" — the Vehicle tile then renders as
 * incomplete instead of showing placeholder values.
 */
data class VehicleDetails(
    val make: String,
    val model: String,
    val registrationNumber: String,
    val fuelType: String,
    val seatingCapacity: Int,
) {
    /** True once every field carries a real (non-blank/non-zero) value. */
    val isComplete: Boolean
        get() = make.isNotBlank() && model.isNotBlank() && registrationNumber.isNotBlank() && fuelType.isNotBlank() && seatingCapacity > 0

    companion object {
        /** Empty starting point for the add/edit sheet when no vehicle is on file yet. */
        val EMPTY =
            VehicleDetails(
                make = "",
                model = "",
                registrationNumber = "",
                fuelType = "",
                seatingCapacity = 0,
            )
    }
}
