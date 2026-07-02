package com.mileway.core.data.settings

import android.content.Context
import java.io.File

/**
 * PLAN_V22 P6.6: Preferences' "Storage" tile's real, on-device cache-size readout + working
 * clear-cache action — replaces its previous
 * `ProfileAction.RaisePreferenceMessage("Manage local data in the full app.")` snackbar-only tap.
 * Reports the Room database file's size plus everything under [Context.cacheDir] (never
 * `filesDir`/DataStore files — those hold real, still-needed local records, not disposable
 * cache), and [clearCache] only ever deletes [Context.cacheDir]'s contents, matching
 * `DebugMenuComposeViewModel.clearAppCache`'s existing pattern.
 */
class StorageRepository(private val context: Context) {
    /** Bytes used by the Room database file (`mileway.db` + its `-wal`/`-shm`/`-journal` siblings, if present). */
    fun databaseBytes(): Long {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        val siblings = dbFile.parentFile?.listFiles { file -> file.name.startsWith(DATABASE_NAME) }.orEmpty()
        return siblings.sumOf { it.length() }
    }

    /** Bytes used by everything under [Context.cacheDir] — the only thing [clearCache] deletes. */
    fun cacheBytes(): Long = directorySizeBytes(context.cacheDir)

    /** Total on-device footprint this screen reports: database + cache. */
    fun totalBytes(): Long = databaseBytes() + cacheBytes()

    /** Deletes everything under [Context.cacheDir] and recreates the (now-empty) directory. */
    fun clearCache() {
        context.cacheDir?.deleteRecursively()
        context.cacheDir?.mkdirs()
    }

    private fun directorySizeBytes(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    companion object {
        private const val DATABASE_NAME = "mileway.db"
    }
}
