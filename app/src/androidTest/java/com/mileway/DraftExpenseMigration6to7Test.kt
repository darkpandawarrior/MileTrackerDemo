package com.mileway

import android.content.Context
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mileway.core.data.database.MIGRATION_6_7
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * P1.5: verifies MIGRATION_6_7 directly — it CREATEs the new single-row `draft_expenses` table
 * so `ExpenseAction.SaveDraft` survives app kill/relaunch. Runs the migration's own SQL against
 * a bare v6 database (no pre-existing `draft_expenses` rows to preserve, unlike an ALTER
 * migration — this one only needs to prove the table exists and is writable afterward).
 * Instrumented (bundled SQLite native lib), runs on the GMD — not the JVM unit-test gate, per
 * `exportSchema=false` blocking `MigrationTestHelper` (memory `miletracker-backlog-audit-v18`).
 */
@RunWith(AndroidJUnit4::class)
class DraftExpenseMigration6to7Test {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration_6_7_test.db"
    private val path get() = context.getDatabasePath(dbName).absolutePath

    @Before
    fun clean() {
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun migration_6_7_creates_draft_expenses_table_and_it_is_writable() {
        val connection = BundledSQLiteDriver().open(path)
        try {
            // Run the migration under test against a bare (no draft_expenses table yet) v6 db.
            MIGRATION_6_7.migrate(connection)

            val columns = mutableSetOf<String>()
            connection.prepare("PRAGMA table_info(`draft_expenses`)").use { stmt ->
                while (stmt.step()) columns += stmt.getText(1) // index 1 = column name
            }
            assertTrue("draftId column exists", "draftId" in columns)
            assertTrue("categoryName column exists", "categoryName" in columns)
            assertTrue("amountText column exists", "amountText" in columns)
            assertTrue("merchantName column exists", "merchantName" in columns)
            assertTrue("note column exists", "note" in columns)
            assertTrue("receiptImagePath column exists", "receiptImagePath" in columns)
            assertTrue("updatedAt column exists", "updatedAt" in columns)

            // The table is writable — a real upsert-then-replace round-trip a client would do.
            connection.execSQL(
                "INSERT INTO `draft_expenses` " +
                    "(`draftId`,`categoryName`,`amountText`,`merchantName`,`note`,`receiptImagePath`,`updatedAt`) " +
                    "VALUES ('expense_draft_singleton','TRAVEL','450.0','Uber: Airport','',NULL,100)",
            )
            connection.execSQL(
                "INSERT OR REPLACE INTO `draft_expenses` " +
                    "(`draftId`,`categoryName`,`amountText`,`merchantName`,`note`,`receiptImagePath`,`updatedAt`) " +
                    "VALUES ('expense_draft_singleton','FOOD','199.0','Cafe Coffee Day','snacks',NULL,200)",
            )
            var count = 0
            connection.prepare("SELECT COUNT(*) FROM `draft_expenses`").use { stmt ->
                if (stmt.step()) count = stmt.getLong(0).toInt()
            }
            // The REPLACE kept it a single row (matches the single-row/singleton draft design).
            assertEquals(1, count)

            var merchantName = ""
            connection.prepare("SELECT `merchantName` FROM `draft_expenses` WHERE `draftId` = 'expense_draft_singleton'").use { stmt ->
                if (stmt.step()) merchantName = stmt.getText(0)
            }
            assertEquals("Cafe Coffee Day", merchantName)
        } finally {
            connection.close()
        }
    }
}
