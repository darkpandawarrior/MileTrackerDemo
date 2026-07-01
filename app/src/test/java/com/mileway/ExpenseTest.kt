package com.mileway

import com.mileway.feature.logging.model.ExpenseCategory
import com.mileway.feature.logging.model.ExpenseRecord
import com.mileway.feature.logging.model.ExpenseStatus
import com.mileway.feature.logging.repository.ExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExpenseTest {

    private val repo = ExpenseRepository()

    @Test
    fun `getAll returns 8 deterministic records`() {
        assertEquals(8, repo.getAll().size)
    }

    @Test
    fun `ids are unique`() {
        val ids = repo.getAll().map { it.id }
        assertEquals(ids.distinct(), ids)
    }

    @Test
    fun `getById returns correct record`() {
        val expense = repo.getById("EXP-001")
        assertNotNull(expense)
        assertEquals("EXP-001", expense.id)
        assertEquals(ExpenseStatus.APPROVED, expense.status)
    }

    @Test
    fun `getById returns null for unknown id`() {
        assertNull(repo.getById("EXP-999"))
    }

    @Test
    fun `filterByStatus null returns all records`() {
        assertEquals(repo.getAll().size, repo.filterByStatus(null).size)
    }

    @Test
    fun `filterByStatus APPROVED returns only approved records`() {
        val approved = repo.filterByStatus(ExpenseStatus.APPROVED)
        assertTrue(approved.isNotEmpty())
        assertTrue(approved.all { it.status == ExpenseStatus.APPROVED })
    }

    @Test
    fun `filterByStatus DRAFT returns only draft records`() {
        val drafts = repo.filterByStatus(ExpenseStatus.DRAFT)
        assertTrue(drafts.isNotEmpty())
        assertTrue(drafts.all { it.status == ExpenseStatus.DRAFT })
    }

    @Test
    fun `all records have positive amounts`() {
        repo.getAll().forEach { expense ->
            assertTrue(expense.amountRupees > 0.0, "${expense.id} has non-positive amount")
        }
    }

    @Test
    fun `requiresApproval is true for amounts over 5000`() {
        val expensive = repo.getAll().filter { it.amountRupees > 5000.0 }
        assertTrue(expensive.isNotEmpty(), "Expected at least one record > ₹5,000")
        expensive.forEach { assertTrue(it.requiresApproval, "${it.id} should require approval") }
    }

    @Test
    fun `requiresApproval is false for amounts at or below 5000`() {
        val cheap = repo.getAll().filter { it.amountRupees <= 5000.0 }
        assertTrue(cheap.isNotEmpty(), "Expected at least one record <= ₹5,000")
        cheap.forEach { assertTrue(!it.requiresApproval, "${it.id} should not require approval") }
    }

    @Test
    fun `getAll is deterministic across two instances`() {
        val repo2 = ExpenseRepository()
        assertEquals(repo.getAll().map { it.id }, repo2.getAll().map { it.id })
    }

    @Test
    fun `all records have non-blank merchant names`() {
        repo.getAll().forEach { expense ->
            assertTrue(expense.merchantName.isNotBlank(), "${expense.id} has blank merchant name")
        }
    }

    @Test
    fun `insert appends a new record and getAll reflects it`() =
        runTest {
            val before = repo.getAll().size
            val new =
                ExpenseRecord(
                    id = "EXP-NEW-1",
                    category = ExpenseCategory.FOOD,
                    merchantName = "Test Cafe",
                    amountRupees = 199.0,
                    status = ExpenseStatus.PENDING,
                    dateMs = 0L,
                )
            repo.insert(new)
            assertEquals(before + 1, repo.getAll().size)
            assertEquals(new, repo.getById("EXP-NEW-1"))
        }

    @Test
    fun `insert with an existing id replaces that record instead of duplicating it`() =
        runTest {
            val before = repo.getAll().size
            val replacement = repo.getById("EXP-001")!!.copy(merchantName = "Updated Merchant")
            repo.insert(replacement)
            assertEquals(before, repo.getAll().size)
            assertEquals("Updated Merchant", repo.getById("EXP-001")?.merchantName)
        }

    @Test
    fun `update replaces an existing record's fields`() =
        runTest {
            val updated = repo.getById("EXP-002")!!.copy(status = ExpenseStatus.APPROVED, amountRupees = 7000.0)
            repo.update(updated)
            val result = repo.getById("EXP-002")
            assertNotNull(result)
            assertEquals(ExpenseStatus.APPROVED, result.status)
            assertEquals(7000.0, result.amountRupees)
        }

    @Test
    fun `update for an unknown id is a no-op`() =
        runTest {
            val before = repo.getAll()
            val ghost =
                ExpenseRecord(
                    id = "EXP-DOES-NOT-EXIST",
                    category = ExpenseCategory.OTHER,
                    merchantName = "Ghost",
                    amountRupees = 1.0,
                    status = ExpenseStatus.DRAFT,
                    dateMs = 0L,
                )
            repo.update(ghost)
            assertEquals(before, repo.getAll())
            assertNull(repo.getById("EXP-DOES-NOT-EXIST"))
        }
}
