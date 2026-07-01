package com.mileway.feature.logging.repository

import com.mileway.feature.logging.model.ExpenseCategory
import com.mileway.feature.logging.model.ExpenseRecord
import com.mileway.feature.logging.model.ExpenseStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExpenseRepository {
    private val baseMs = 1_700_000_000_000L
    private val dayMs = 86_400_000L

    private val seedRecords =
        listOf(
            ExpenseRecord(
                id = "EXP-001",
                category = ExpenseCategory.FOOD,
                merchantName = "Swiggy: Team Lunch",
                amountRupees = 1850.0,
                status = ExpenseStatus.APPROVED,
                dateMs = baseMs - 1 * dayMs,
                note = "Team lunch for Q3 review meeting",
            ),
            ExpenseRecord(
                id = "EXP-002",
                category = ExpenseCategory.TRAVEL,
                merchantName = "Ola Cabs: Airport",
                amountRupees = 6200.0,
                status = ExpenseStatus.PENDING,
                dateMs = baseMs - 2 * dayMs,
                note = "Client visit: Bengaluru",
            ),
            ExpenseRecord(
                id = "EXP-003",
                category = ExpenseCategory.ACCOMMODATION,
                merchantName = "Taj Hotel: 1 Night",
                amountRupees = 8900.0,
                status = ExpenseStatus.APPROVED,
                dateMs = baseMs - 4 * dayMs,
                note = "Outstation client meeting",
            ),
            ExpenseRecord(
                id = "EXP-004",
                category = ExpenseCategory.OFFICE_SUPPLIES,
                merchantName = "Staples: Stationery",
                amountRupees = 340.0,
                status = ExpenseStatus.DRAFT,
                dateMs = baseMs - 5 * dayMs,
            ),
            ExpenseRecord(
                id = "EXP-005",
                category = ExpenseCategory.COMMUNICATION,
                merchantName = "Airtel Postpaid Bill",
                amountRupees = 1199.0,
                status = ExpenseStatus.APPROVED,
                dateMs = baseMs - 8 * dayMs,
                note = "Corporate SIM monthly bill",
            ),
            ExpenseRecord(
                id = "EXP-006",
                category = ExpenseCategory.MEDICAL,
                merchantName = "Apollo Pharmacy",
                amountRupees = 620.0,
                status = ExpenseStatus.PENDING,
                dateMs = baseMs - 10 * dayMs,
                note = "First aid supplies for office",
            ),
            ExpenseRecord(
                id = "EXP-007",
                category = ExpenseCategory.FOOD,
                merchantName = "The Bombay Canteen: Client Dinner",
                amountRupees = 4750.0,
                status = ExpenseStatus.REJECTED,
                dateMs = baseMs - 14 * dayMs,
                note = "Q3 client entertainment",
            ),
            ExpenseRecord(
                id = "EXP-008",
                category = ExpenseCategory.TRAVEL,
                merchantName = "IndiGo Airlines",
                amountRupees = 12500.0,
                status = ExpenseStatus.PENDING,
                dateMs = baseMs - 18 * dayMs,
                note = "Mumbai–Delhi flight for annual summit",
            ),
        )

    private val _recordsFlow = MutableStateFlow(seedRecords)
    val recordsFlow: StateFlow<List<ExpenseRecord>> = _recordsFlow.asStateFlow()

    private val records: List<ExpenseRecord> get() = _recordsFlow.value

    fun getAll(): List<ExpenseRecord> = records

    fun getById(id: String): ExpenseRecord? = records.find { it.id == id }

    fun filterByStatus(status: ExpenseStatus?): List<ExpenseRecord> = if (status == null) records else records.filter { it.status == status }

    /** Appends a new record, or replaces it if an entry with the same [ExpenseRecord.id] already exists. */
    suspend fun insert(record: ExpenseRecord) {
        _recordsFlow.value =
            if (records.any { it.id == record.id }) {
                records.map { if (it.id == record.id) record else it }
            } else {
                records + record
            }
    }

    /** Replaces an existing record matching [ExpenseRecord.id]; a no-op if no such record exists. */
    suspend fun update(record: ExpenseRecord) {
        _recordsFlow.value = records.map { if (it.id == record.id) record else it }
    }
}
