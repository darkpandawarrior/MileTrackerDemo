package com.mileway.feature.logging.validation

import com.mileway.feature.logging.catalog.ExpenseCategoryCatalog
import com.mileway.feature.logging.model.ExpenseCategory
import com.mileway.feature.logging.viewmodel.ExpenseFormState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpenseFormValidatorTest {
    private val travelDef = ExpenseCategoryCatalog.default().first { it.category == ExpenseCategory.TRAVEL }

    @Test
    fun `valid form has no errors`() {
        val form =
            ExpenseFormState(
                category = ExpenseCategory.TRAVEL,
                amountText = "500",
                merchantName = "Ola Cabs",
            )
        val errors = ExpenseFormValidator.validate(form, travelDef)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `blank merchant name produces merchant error only`() {
        val form =
            ExpenseFormState(
                category = ExpenseCategory.TRAVEL,
                amountText = "500",
                merchantName = "   ",
            )
        val errors = ExpenseFormValidator.validate(form, travelDef)
        assertEquals(setOf(ExpenseFormValidator.FIELD_MERCHANT_NAME), errors.keys)
    }

    @Test
    fun `zero amount produces amount error`() {
        val form =
            ExpenseFormState(
                category = ExpenseCategory.TRAVEL,
                amountText = "0",
                merchantName = "Ola Cabs",
            )
        val errors = ExpenseFormValidator.validate(form, travelDef)
        assertEquals(setOf(ExpenseFormValidator.FIELD_AMOUNT), errors.keys)
    }

    @Test
    fun `negative amount produces amount error`() {
        val form =
            ExpenseFormState(
                category = ExpenseCategory.TRAVEL,
                amountText = "-50",
                merchantName = "Ola Cabs",
            )
        val errors = ExpenseFormValidator.validate(form, travelDef)
        assertEquals(setOf(ExpenseFormValidator.FIELD_AMOUNT), errors.keys)
    }

    @Test
    fun `non-numeric amount text produces amount error`() {
        val form =
            ExpenseFormState(
                category = ExpenseCategory.TRAVEL,
                amountText = "abc",
                merchantName = "Ola Cabs",
            )
        val errors = ExpenseFormValidator.validate(form, travelDef)
        assertEquals(setOf(ExpenseFormValidator.FIELD_AMOUNT), errors.keys)
    }

    @Test
    fun `missing category produces category error`() {
        val form =
            ExpenseFormState(
                category = null,
                amountText = "500",
                merchantName = "Ola Cabs",
            )
        val errors = ExpenseFormValidator.validate(form, null)
        assertEquals(setOf(ExpenseFormValidator.FIELD_CATEGORY), errors.keys)
    }

    @Test
    fun `all fields blank produces all three errors`() {
        val errors = ExpenseFormValidator.validate(ExpenseFormState(), null)
        assertEquals(
            setOf(
                ExpenseFormValidator.FIELD_CATEGORY,
                ExpenseFormValidator.FIELD_MERCHANT_NAME,
                ExpenseFormValidator.FIELD_AMOUNT,
            ),
            errors.keys,
        )
    }
}
