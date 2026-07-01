package com.mileway.feature.logging.import

import com.mileway.feature.logging.model.DraftStatus
import com.mileway.feature.logging.model.ExpenseCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpenseCsvImporterTest {
    @Test
    fun `blank text produces no rows`() {
        assertEquals(emptyList(), ExpenseCsvImporter.parse(""))
        assertEquals(emptyList(), ExpenseCsvImporter.parse("   \n  \n"))
    }

    @Test
    fun `well-formed 5-row CSV produces 5 PENDING rows with resolved fields`() {
        val csv =
            """
            category,amount,merchant,note
            Food,100,Cafe A,Lunch
            Travel,250,Ola Cabs,Airport drop
            Accommodation,4200,Taj Hotel,
            Office Supplies,899,Staples,Pens
            Other,50,Misc Vendor,
            """.trimIndent()

        val rows = ExpenseCsvImporter.parse(csv)

        assertEquals(5, rows.size)
        assertTrue(rows.all { it.status == DraftStatus.PENDING })
        assertEquals(ExpenseCategory.FOOD, rows[0].category)
        assertEquals("100", rows[0].amountText)
        assertEquals("Cafe A", rows[0].merchantName)
        assertEquals("Lunch", rows[0].note)
        assertEquals(ExpenseCategory.TRAVEL, rows[1].category)
        assertEquals(ExpenseCategory.ACCOMMODATION, rows[2].category)
        assertEquals(ExpenseCategory.OFFICE_SUPPLIES, rows[3].category)
        assertEquals(ExpenseCategory.OTHER, rows[4].category)
        // Distinct ids across rows.
        assertEquals(rows.size, rows.map { it.id }.toSet().size)
    }

    @Test
    fun `header matching is case-insensitive and column order agnostic`() {
        val csv =
            """
            Merchant,Category,Amount
            Cafe A,food,100
            """.trimIndent()

        val rows = ExpenseCsvImporter.parse(csv)

        assertEquals(1, rows.size)
        assertEquals("Cafe A", rows[0].merchantName)
        assertEquals(ExpenseCategory.FOOD, rows[0].category)
        assertEquals("100", rows[0].amountText)
        assertEquals(DraftStatus.PENDING, rows[0].status)
    }

    @Test
    fun `tab-delimited input is auto-detected and parsed`() {
        val tsv = "category\tamount\tmerchant\tnote\nFood\t100\tCafe A\tLunch"

        val rows = ExpenseCsvImporter.parse(tsv)

        assertEquals(1, rows.size)
        assertEquals(ExpenseCategory.FOOD, rows[0].category)
        assertEquals("100", rows[0].amountText)
        assertEquals("Cafe A", rows[0].merchantName)
    }

    @Test
    fun `unrecognized category falls back to OTHER instead of failing the row`() {
        val csv = "category,amount,merchant,note\nSpaceship Fuel,100,Cafe A,"

        val rows = ExpenseCsvImporter.parse(csv)

        assertEquals(1, rows.size)
        assertEquals(ExpenseCategory.OTHER, rows[0].category)
        assertEquals(DraftStatus.PENDING, rows[0].status)
    }

    @Test
    fun `a file with one malformed row surfaces exactly one ERROR row plus 4 valid ones`() {
        val csv =
            """
            category,amount,merchant,note
            Food,100,Cafe A,
            Travel,not-a-number,Ola Cabs,
            Accommodation,4200,Taj Hotel,
            Office Supplies,899,Staples,
            Other,50,Misc Vendor,
            """.trimIndent()

        val rows = ExpenseCsvImporter.parse(csv)

        assertEquals(5, rows.size)
        val errorRows = rows.filter { it.status == DraftStatus.ERROR }
        val pendingRows = rows.filter { it.status == DraftStatus.PENDING }
        assertEquals(1, errorRows.size)
        assertEquals(4, pendingRows.size)
        assertEquals("Ola Cabs", errorRows.first().merchantName)
    }

    @Test
    fun `a row with a blank merchant name is malformed even with a valid amount`() {
        val csv = "category,amount,merchant,note\nFood,100,,"

        val rows = ExpenseCsvImporter.parse(csv)

        assertEquals(1, rows.size)
        assertEquals(DraftStatus.ERROR, rows[0].status)
    }

    @Test
    fun `a row with a zero or negative amount is malformed`() {
        val csv =
            """
            category,amount,merchant,note
            Food,0,Cafe A,
            Food,-10,Cafe B,
            """.trimIndent()

        val rows = ExpenseCsvImporter.parse(csv)

        assertEquals(2, rows.size)
        assertTrue(rows.all { it.status == DraftStatus.ERROR })
    }
}
