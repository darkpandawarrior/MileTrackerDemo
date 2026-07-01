package com.mileway.feature.logging.catalog

import com.mileway.feature.logging.model.ExpenseCategory
import com.mileway.feature.logging.model.ExpenseCategoryDef

/**
 * Local, offline provider of expense category definitions. Replaces a hardcoded per-category
 * `when` with a single source of truth the entry grid and form validator both read from, so a
 * category's required-field set (receipt / cost center / GST) can change without a new screen.
 */
object ExpenseCategoryCatalog {
    fun default(): List<ExpenseCategoryDef> =
        listOf(
            ExpenseCategoryDef(
                category = ExpenseCategory.FOOD,
                requiresReceipt = false,
                requiresCostCenter = false,
                requiresGst = false,
            ),
            ExpenseCategoryDef(
                category = ExpenseCategory.TRAVEL,
                requiresReceipt = true,
                requiresCostCenter = true,
                requiresGst = false,
            ),
            ExpenseCategoryDef(
                category = ExpenseCategory.ACCOMMODATION,
                requiresReceipt = true,
                requiresCostCenter = true,
                requiresGst = true,
            ),
            ExpenseCategoryDef(
                category = ExpenseCategory.OFFICE_SUPPLIES,
                requiresReceipt = true,
                requiresCostCenter = true,
                requiresGst = true,
            ),
            ExpenseCategoryDef(
                category = ExpenseCategory.COMMUNICATION,
                requiresReceipt = false,
                requiresCostCenter = false,
                requiresGst = false,
            ),
            ExpenseCategoryDef(
                category = ExpenseCategory.MEDICAL,
                requiresReceipt = true,
                requiresCostCenter = false,
                requiresGst = false,
            ),
            ExpenseCategoryDef(
                category = ExpenseCategory.OTHER,
                requiresReceipt = false,
                requiresCostCenter = false,
                requiresGst = false,
            ),
        )
}
