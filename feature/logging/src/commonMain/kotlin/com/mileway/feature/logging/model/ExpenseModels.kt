package com.mileway.feature.logging.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(val label: String, val icon: ImageVector) {
    FOOD("Food", Icons.Filled.Restaurant),
    TRAVEL("Travel", Icons.Filled.DirectionsCar),
    ACCOMMODATION("Accommodation", Icons.Filled.Hotel),
    OFFICE_SUPPLIES("Office Supplies", Icons.Filled.Receipt),
    COMMUNICATION("Communication", Icons.Filled.PhoneAndroid),
    MEDICAL("Medical", Icons.Filled.LocalHospital),
    OTHER("Other", Icons.Filled.Category),
}

enum class ExpenseStatus { DRAFT, PENDING, APPROVED, REJECTED }

/**
 * Per-category field requirements, sourced from [com.mileway.feature.logging.catalog.ExpenseCategoryCatalog]
 * rather than hardcoded per-screen `when` branches. Lets a category gain a required field (receipt,
 * cost center, GST) without a new screen.
 */
data class ExpenseCategoryDef(
    val category: ExpenseCategory,
    val requiresReceipt: Boolean = false,
    val requiresCostCenter: Boolean = false,
    val requiresGst: Boolean = false,
)

data class ExpenseRecord(
    val id: String,
    val category: ExpenseCategory,
    val merchantName: String,
    val amountRupees: Double,
    val status: ExpenseStatus,
    val dateMs: Long,
    val note: String = "",
) {
    val requiresApproval: Boolean get() = amountRupees > 5000.0
}
