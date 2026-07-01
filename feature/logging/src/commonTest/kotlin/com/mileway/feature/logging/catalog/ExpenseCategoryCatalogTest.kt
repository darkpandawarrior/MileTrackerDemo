package com.mileway.feature.logging.catalog

import com.mileway.feature.logging.model.ExpenseCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpenseCategoryCatalogTest {
    @Test
    fun `default catalog has one entry per existing category`() {
        val defs = ExpenseCategoryCatalog.default()
        assertEquals(ExpenseCategory.entries.size, defs.size)
        assertEquals(ExpenseCategory.entries.toSet(), defs.map { it.category }.toSet())
    }

    @Test
    fun `travel requires receipt and cost center but not gst`() {
        val def = ExpenseCategoryCatalog.default().first { it.category == ExpenseCategory.TRAVEL }
        assertTrue(def.requiresReceipt)
        assertTrue(def.requiresCostCenter)
        assertFalse(def.requiresGst)
    }

    @Test
    fun `accommodation requires receipt cost center and gst`() {
        val def = ExpenseCategoryCatalog.default().first { it.category == ExpenseCategory.ACCOMMODATION }
        assertTrue(def.requiresReceipt)
        assertTrue(def.requiresCostCenter)
        assertTrue(def.requiresGst)
    }

    @Test
    fun `office supplies requires receipt cost center and gst`() {
        val def = ExpenseCategoryCatalog.default().first { it.category == ExpenseCategory.OFFICE_SUPPLIES }
        assertTrue(def.requiresReceipt)
        assertTrue(def.requiresCostCenter)
        assertTrue(def.requiresGst)
    }

    @Test
    fun `medical requires only receipt`() {
        val def = ExpenseCategoryCatalog.default().first { it.category == ExpenseCategory.MEDICAL }
        assertTrue(def.requiresReceipt)
        assertFalse(def.requiresCostCenter)
        assertFalse(def.requiresGst)
    }

    @Test
    fun `food communication and other require nothing extra`() {
        val defs = ExpenseCategoryCatalog.default()
        listOf(ExpenseCategory.FOOD, ExpenseCategory.COMMUNICATION, ExpenseCategory.OTHER).forEach { category ->
            val def = defs.first { it.category == category }
            assertFalse(def.requiresReceipt, "$category should not require receipt")
            assertFalse(def.requiresCostCenter, "$category should not require cost center")
            assertFalse(def.requiresGst, "$category should not require gst")
        }
    }
}
