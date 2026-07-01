package com.mileway.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migration 11 → 12 (PLAN_V22 P6.2): additive `vehicle_details` + `passport_details` singleton
 * tables backing the Profile Details screen's new Vehicle/Passport tiles. Both are single-row
 * tables (fixed primary key, same pattern as `draft_expenses`) — one vehicle and one passport per
 * demo profile.
 */
val MIGRATION_11_12 =
    object : Migration(11, 12) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `vehicle_details` (
                    `id`                 TEXT    NOT NULL PRIMARY KEY,
                    `make`               TEXT    NOT NULL,
                    `model`              TEXT    NOT NULL,
                    `registrationNumber` TEXT    NOT NULL,
                    `fuelType`           TEXT    NOT NULL,
                    `seatingCapacity`    INTEGER NOT NULL,
                    `updatedAtMs`        INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `passport_details` (
                    `id`                TEXT    NOT NULL PRIMARY KEY,
                    `passportNumber`    TEXT    NOT NULL,
                    `issuingCountry`    TEXT    NOT NULL,
                    `expiryDateMillis`  INTEGER NOT NULL,
                    `updatedAtMs`       INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

/**
 * Migration 10 → 11 (PLAN_V22 P1.1): additive `mock_accounts` table — a real, persisted,
 * seedable multi-persona account store, replacing `stub.ProfileMockData.accounts()`'s bare
 * in-memory list as the source of truth for account switching. See
 * [com.mileway.core.data.model.db.MockAccountEntity].
 */
val MIGRATION_10_11 =
    object : Migration(10, 11) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `mock_accounts` (
                    `accountId`     TEXT    NOT NULL PRIMARY KEY,
                    `displayName`   TEXT    NOT NULL,
                    `employeeCode`  TEXT    NOT NULL,
                    `organization`  TEXT    NOT NULL,
                    `avatarSeed`    TEXT    NOT NULL,
                    `isActive`      INTEGER NOT NULL,
                    `lastLoginAtMs` INTEGER NOT NULL,
                    `createdAtMs`   INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

/**
 * Migration 9 → 10 (P3.4): `vouchers.category` becomes the typed [com.mileway.core.data.model.db
 * .VoucherCategory] enum instead of a free-text `String` — but the underlying column is still
 * `TEXT` storing the exact same label values (`"Travel"`/`"Fuel"`/`"Maintenance"`/`"Other"`) via
 * [com.mileway.core.data.model.db.VoucherCategoryConverters], so no column type or existing row
 * data actually changes. This migration is a documented no-op version bump only, matching the
 * project rule that any entity-shape change to an already-shipped table gets an explicit
 * `Migration(N, N + 1)` object rather than relying on `exportSchema`/destructive fallback.
 */
val MIGRATION_9_10 =
    object : Migration(9, 10) {
        override fun migrate(connection: SQLiteConnection) {
            // No-op: the `category` column's on-disk type/values are unchanged (see class doc).
        }
    }

/**
 * Migration 8 → 9 (P3.3): additive `claimedByVoucherNumber` column on `saved_tracks` — the
 * already-claimed guard so the same completed trip can't fund two separate vouchers (the reference app's
 * server-side equivalent: `remainingVoucherCountForTrip`). Null means "unclaimed"; existing rows
 * default to null, so nothing already-submitted becomes newly claimed by this migration alone.
 */
val MIGRATION_8_9 =
    object : Migration(8, 9) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `saved_tracks` ADD COLUMN `claimedByVoucherNumber` TEXT")
        }
    }

/**
 * Migration 7 → 8 (P3.1): shared `vouchers` table — the single Room-backed store both
 * `CreateVoucherViewModel` (feature/tracking) and `VoucherHistoryViewModel` (feature/logging)
 * bind to via Koin, replacing two disconnected in-memory stores. See
 * [com.mileway.core.data.model.db.VoucherEntity].
 */
val MIGRATION_7_8 =
    object : Migration(7, 8) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `vouchers` (
                    `voucherNumber`        TEXT    NOT NULL PRIMARY KEY,
                    `title`                TEXT    NOT NULL,
                    `category`             TEXT    NOT NULL,
                    `totalAmount`          REAL    NOT NULL,
                    `notes`                TEXT    NOT NULL,
                    `expenseRouteIdsJson`  TEXT    NOT NULL,
                    `status`               TEXT    NOT NULL,
                    `createdAtMs`          INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

/**
 * Migration 6 → 7 (P1.5): single-row `draft_expenses` table so `ExpenseAction.SaveDraft`
 * actually persists across app kill/relaunch instead of only living in `ExpenseViewModel`
 * state. Mirrors `ExpenseFormState`'s fields (see [com.mileway.core.data.model.db.DraftExpenseEntity]).
 */
val MIGRATION_6_7 =
    object : Migration(6, 7) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `draft_expenses` (
                    `draftId`          TEXT    NOT NULL PRIMARY KEY,
                    `categoryName`     TEXT,
                    `amountText`       TEXT    NOT NULL,
                    `merchantName`     TEXT    NOT NULL,
                    `note`             TEXT    NOT NULL,
                    `receiptImagePath` TEXT,
                    `updatedAt`        INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

/**
 * Migration 5 → 6 (P1.1): agent conversations + messages tables for persistent chat history.
 */
val MIGRATION_5_6 =
    object : Migration(5, 6) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `agent_conversations` (
                    `id`            TEXT    NOT NULL PRIMARY KEY,
                    `title`         TEXT    NOT NULL,
                    `lastMessageMs` INTEGER NOT NULL,
                    `createdAtMs`   INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `agent_messages` (
                    `messageId`       TEXT    NOT NULL PRIMARY KEY,
                    `conversationId`  TEXT    NOT NULL,
                    `text`            TEXT    NOT NULL,
                    `isUser`          INTEGER NOT NULL,
                    `timestampMs`     INTEGER NOT NULL,
                    `feedbackRating`  INTEGER,
                    `feedbackComment` TEXT,
                    `module`          TEXT,
                    `isContextReset`  INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(`conversationId`) REFERENCES `agent_conversations`(`id`) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_agent_messages_conversationId` ON `agent_messages` (`conversationId`)",
            )
        }
    }

/**
 * Migration 4 → 5 (D.5): enrich trip_attachments with a file name + multi-pass OCR provenance
 * (confidence + verified flag from [com.mileway.feature.media.ocr.OdometerOcrAggregator]).
 * Plain additive ALTERs; existing rows keep the column defaults.
 */
val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `trip_attachments` ADD COLUMN `file_name` TEXT")
            connection.execSQL("ALTER TABLE `trip_attachments` ADD COLUMN `ocr_confidence` REAL NOT NULL DEFAULT 0")
            connection.execSQL("ALTER TABLE `trip_attachments` ADD COLUMN `ocr_verified` INTEGER NOT NULL DEFAULT 0")
        }
    }

/**
 * Migration 1 → 2: introduce the trip_attachments table.
 *
 * Each row stores one photo attachment (receipt or odometer proof) keyed by the
 * track_token (= SavedTrack.routeId). The type column uses the AttachmentType enum
 * name (TEXT): RECEIPT, ODOMETER_START, ODOMETER_END.
 */
val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `submit_drafts` (
                    `formKey`      TEXT    NOT NULL,
                    `uniqueKey`    TEXT    NOT NULL,
                    `payloadJson`  TEXT    NOT NULL,
                    `status`       TEXT    NOT NULL,
                    `errorMessage` TEXT,
                    `createdAt`    INTEGER NOT NULL,
                    `updatedAt`    INTEGER NOT NULL,
                    PRIMARY KEY(`formKey`, `uniqueKey`)
                )
                """.trimIndent(),
            )
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `media_library` (
                    `id`        TEXT    NOT NULL PRIMARY KEY,
                    `uri`       TEXT    NOT NULL,
                    `mimeType`  TEXT    NOT NULL,
                    `label`     TEXT    NOT NULL,
                    `source`    TEXT    NOT NULL,
                    `savedAtMs` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `trip_attachments` (
                    `id`          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    `track_token` TEXT    NOT NULL,
                    `type`        TEXT    NOT NULL,
                    `uri`         TEXT    NOT NULL,
                    `ocr_text`    TEXT,
                    `created_at`  INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_trip_attachments_track_token` ON `trip_attachments` (`track_token`)",
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_trip_attachments_track_token_type` ON `trip_attachments` (`track_token`, `type`)",
            )
        }
    }
