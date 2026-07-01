package com.mileway.feature.profile.model

/**
 * P6.2: the passport linked to this profile — mirrors the reference app's passport field shape
 * (number, issuing country, expiry) at the data level; rendering is Mileway's own tile + bottom
 * sheet, never a port of the reference app's UI. Room-backed (single-row table, same pattern as
 * [com.mileway.core.data.model.db.DraftExpenseEntity]) so edits persist across app restart. `null`
 * from the repository means "not yet set" — the Passport tile then renders as incomplete instead
 * of showing placeholder values.
 */
data class PassportDetails(
    val passportNumber: String,
    val issuingCountry: String,
    val expiryDateMillis: Long,
) {
    /** True once every field carries a real (non-blank/set) value. */
    val isComplete: Boolean
        get() = passportNumber.isNotBlank() && issuingCountry.isNotBlank() && expiryDateMillis > 0L

    companion object {
        /** Empty starting point for the add/edit sheet when no passport is on file yet. */
        val EMPTY =
            PassportDetails(
                passportNumber = "",
                issuingCountry = "",
                expiryDateMillis = 0L,
            )
    }
}
