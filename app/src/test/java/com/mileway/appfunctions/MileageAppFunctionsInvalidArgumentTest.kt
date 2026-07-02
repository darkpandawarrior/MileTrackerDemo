package com.mileway.appfunctions

import android.app.Application
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionInvalidArgumentException
import com.mileway.FakeSnapshotCache
import com.mileway.feature.logging.repository.ExpenseRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFailsWith

/**
 * P7.5: split out from [MileageAppFunctionsTest] because `AppFunctionInvalidArgumentException`'s
 * single-arg constructor reads the real `android.os.Bundle.EMPTY` static field internally — under
 * a plain (non-Robolectric) unit test the stub Android jar leaves that field null, so only a
 * Robolectric-shadowed `Bundle` lets this framework exception class be constructed at all. Kept in
 * its own minimal class so the rest of the suite doesn't pay Robolectric's setup cost.
 *
 * `@Config(application = Application::class)` overrides Robolectric's default of instantiating
 * the manifest's real `MilewayApplication` (which would run the full `onCreate()`/Koin/Room
 * bootstrap this test has nothing to do with, and which fails here for an unrelated reason: this
 * JVM has no bundled-SQLite native lib for Robolectric). A plain [Application] skips all of that.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class MileageAppFunctionsInvalidArgumentTest {
    @Test
    fun `logExpense rejects a non-positive amount`() =
        runTest {
            val ctx: AppFunctionContext = mockk(relaxed = true)
            val functions = MileageAppFunctions(mockk(relaxed = true), ExpenseRepository(), FakeSnapshotCache())

            assertFailsWith<AppFunctionInvalidArgumentException> {
                functions.logExpense(ctx, category = "Food", amountRupees = 0.0)
            }
        }
}
