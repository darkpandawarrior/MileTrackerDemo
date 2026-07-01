package com.mileway

import android.content.Context
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mileway.core.data.database.MIGRATION_10_11
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PLAN_V22 P1.1: verifies MIGRATION_10_11 directly — it CREATEs the new additive `mock_accounts`
 * table backing the Room-backed multi-persona account store. Runs the migration's own SQL against
 * a bare v10 database (no pre-existing `mock_accounts` rows to preserve, matching the "additive
 * table only" shape). Instrumented (bundled SQLite native lib), runs on the GMD — not the JVM
 * unit-test gate, per `exportSchema=false` blocking `MigrationTestHelper` (memory
 * `miletracker-backlog-audit-v18`).
 */
@RunWith(AndroidJUnit4::class)
class MockAccountMigration10to11Test {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration_10_11_test.db"
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
    fun migration_10_11_creates_mock_accounts_table_and_it_is_writable() {
        val connection = BundledSQLiteDriver().open(path)
        try {
            // Run the migration under test against a bare (no mock_accounts table yet) v10 db.
            MIGRATION_10_11.migrate(connection)

            val columns = mutableSetOf<String>()
            connection.prepare("PRAGMA table_info(`mock_accounts`)").use { stmt ->
                while (stmt.step()) columns += stmt.getText(1) // index 1 = column name
            }
            assertTrue("accountId column exists", "accountId" in columns)
            assertTrue("displayName column exists", "displayName" in columns)
            assertTrue("employeeCode column exists", "employeeCode" in columns)
            assertTrue("organization column exists", "organization" in columns)
            assertTrue("avatarSeed column exists", "avatarSeed" in columns)
            assertTrue("isActive column exists", "isActive" in columns)
            assertTrue("lastLoginAtMs column exists", "lastLoginAtMs" in columns)
            assertTrue("createdAtMs column exists", "createdAtMs" in columns)

            // The table is writable — a real seed-then-switch round-trip a client would do.
            connection.execSQL(
                "INSERT INTO `mock_accounts` " +
                    "(`accountId`,`displayName`,`employeeCode`,`organization`,`avatarSeed`,`isActive`,`lastLoginAtMs`,`createdAtMs`) " +
                    "VALUES ('ACC-001','Demo User','EMP001','Demo Logistics Pvt Ltd','ACC-001',1,100,100)",
            )
            connection.execSQL(
                "INSERT INTO `mock_accounts` " +
                    "(`accountId`,`displayName`,`employeeCode`,`organization`,`avatarSeed`,`isActive`,`lastLoginAtMs`,`createdAtMs`) " +
                    "VALUES ('ACC-002','QA Tester','QA042','Demo QA Workspace','ACC-002',0,200,200)",
            )
            var count = 0
            connection.prepare("SELECT COUNT(*) FROM `mock_accounts`").use { stmt ->
                if (stmt.step()) count = stmt.getLong(0).toInt()
            }
            assertEquals(2, count)

            // setActive's clear-then-set shape, expressed as raw SQL (the DAO's @Transaction
            // wraps exactly these two statements).
            connection.execSQL("UPDATE `mock_accounts` SET `isActive` = 0")
            connection.execSQL("UPDATE `mock_accounts` SET `isActive` = 1 WHERE `accountId` = 'ACC-002'")

            var activeCount = 0
            connection.prepare("SELECT COUNT(*) FROM `mock_accounts` WHERE `isActive` = 1").use { stmt ->
                if (stmt.step()) activeCount = stmt.getLong(0).toInt()
            }
            assertEquals(1, activeCount)

            var activeId = ""
            connection.prepare("SELECT `accountId` FROM `mock_accounts` WHERE `isActive` = 1").use { stmt ->
                if (stmt.step()) activeId = stmt.getText(0)
            }
            assertEquals("ACC-002", activeId)
        } finally {
            connection.close()
        }
    }
}
