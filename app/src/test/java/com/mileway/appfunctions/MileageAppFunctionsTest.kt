package com.mileway.appfunctions

import androidx.appfunctions.AppFunctionContext
import com.mileway.FakeSnapshotCache
import com.mileway.core.data.watch.WatchSyncPayload
import com.mileway.feature.logging.model.ExpenseStatus
import com.mileway.feature.logging.repository.ExpenseRepository
import com.mileway.feature.tracking.watch.WatchFacade
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * P7.5: unit coverage for [MileageAppFunctions]'s own logic (Acceptance clause). Delegation to
 * [WatchFacade] for start/stop is already covered end-to-end by `feature:tracking`'s
 * `WatchFacadeTest`, so a relaxed mock verifies only that this class calls through correctly —
 * [logExpense]'s category resolution/approval threshold and [getTodaySummary]'s today-filter are
 * the new logic this task adds, and are exercised against the real [ExpenseRepository].
 *
 * The non-positive-amount rejection path is covered separately in
 * [MileageAppFunctionsInvalidArgumentTest] (needs Robolectric — see that class's doc) so the rest
 * of this suite stays fast plain-JUnit.
 */
class MileageAppFunctionsTest {
    private val ctx: AppFunctionContext = mockk(relaxed = true)

    @Test
    fun `startTrackingTrip delegates to WatchFacade`() =
        runTest {
            val facade = mockk<WatchFacade>(relaxed = true)
            val functions = MileageAppFunctions(facade, ExpenseRepository(), FakeSnapshotCache())

            functions.startTrackingTrip(ctx)

            coVerify(exactly = 1) { facade.startTracking() }
        }

    @Test
    fun `stopTrackingTrip delegates to WatchFacade`() =
        runTest {
            val facade = mockk<WatchFacade>(relaxed = true)
            val functions = MileageAppFunctions(facade, ExpenseRepository(), FakeSnapshotCache())

            functions.stopTrackingTrip(ctx)

            coVerify(exactly = 1) { facade.stopTracking() }
        }

    @Test
    fun `logExpense resolves a known category and stays pending under the approval threshold`() =
        runTest {
            val repo = ExpenseRepository()
            val functions = MileageAppFunctions(mockk(relaxed = true), repo, FakeSnapshotCache())

            val result = functions.logExpense(ctx, category = "Food", amountRupees = 500.0, merchantName = "Cafe")

            assertEquals("Food", result.category)
            assertEquals(ExpenseStatus.PENDING.name, result.status)
            assertFalse(result.requiresApproval)
            assertTrue(repo.getAll().any { it.id == result.id })
        }

    @Test
    fun `logExpense above the threshold requires approval`() =
        runTest {
            val functions = MileageAppFunctions(mockk(relaxed = true), ExpenseRepository(), FakeSnapshotCache())

            val result = functions.logExpense(ctx, category = "Travel", amountRupees = 6000.0)

            assertTrue(result.requiresApproval)
        }

    @Test
    fun `logExpense falls back to Other for an unrecognized category`() =
        runTest {
            val functions = MileageAppFunctions(mockk(relaxed = true), ExpenseRepository(), FakeSnapshotCache())

            val result = functions.logExpense(ctx, category = "Not A Real Category", amountRupees = 100.0)

            assertEquals("Other", result.category)
        }

    @Test
    fun `getTodaySummary reflects the cached tracking payload and today's logged expenses`() =
        runTest {
            val repo = ExpenseRepository()
            val cache = FakeSnapshotCache(WatchSyncPayload(todayKm = 12.5, isTracking = true))
            val functions = MileageAppFunctions(mockk(relaxed = true), repo, cache)

            functions.logExpense(ctx, category = "Food", amountRupees = 200.0)
            val summary = functions.getTodaySummary(ctx)

            assertEquals(12.5, summary.distanceKm)
            assertTrue(summary.isTracking)
            assertEquals(200.0, summary.expensesTodayRupees)
            assertEquals(1, summary.expenseCountToday)
        }

    @Test
    fun `getTodaySummary excludes expenses logged before today`() =
        runTest {
            val repo = ExpenseRepository()
            val yesterdayMs = System.currentTimeMillis() - 2 * 86_400_000L
            repo.insert(
                com.mileway.feature.logging.model.ExpenseRecord(
                    id = "EXP-OLD",
                    category = com.mileway.feature.logging.model.ExpenseCategory.FOOD,
                    merchantName = "Old",
                    amountRupees = 999.0,
                    status = ExpenseStatus.APPROVED,
                    dateMs = yesterdayMs,
                ),
            )
            val functions = MileageAppFunctions(mockk(relaxed = true), repo, FakeSnapshotCache())

            val summary = functions.getTodaySummary(ctx)

            assertEquals(0, summary.expenseCountToday)
            assertEquals(0.0, summary.expensesTodayRupees)
        }
}
