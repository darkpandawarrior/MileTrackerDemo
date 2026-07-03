package com.mileway

import android.content.Context
import com.mileway.core.data.settings.StorageRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * P6.6: [StorageRepository]'s byte-count readout + clear-cache action, backing Preferences'
 * Storage tile/sheet.
 *
 * Runs on a plain JVM with a mocked [Context] over real [TemporaryFolder] directories rather than
 * under Robolectric: [StorageRepository] only touches `cacheDir` + `getDatabasePath`, and doing
 * real cacheDir I/O under Robolectric corrupted its managed temp-dir bookkeeping (Z.5b — a Linux-CI
 * `NoSuchFileException` in the next test method's sandbox setup). A TemporaryFolder is exactly the
 * isolated, disposable filesystem this needs.
 */
class StorageRepositoryTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var cacheDir: File
    private lateinit var dbDir: File

    private fun repository(): StorageRepository {
        cacheDir = tmp.newFolder("cache")
        dbDir = tmp.newFolder("databases")
        val context =
            mockk<Context> {
                every { cacheDir } returns this@StorageRepositoryTest.cacheDir
                every { getDatabasePath(any()) } answers { File(dbDir, firstArg()) }
            }
        return StorageRepository(context)
    }

    @Test
    fun `cacheBytes reflects files written under cacheDir`() {
        val repository = repository()
        assertEquals(0L, repository.cacheBytes())

        File(cacheDir, "demo.tmp").writeBytes(ByteArray(2_048))

        assertEquals(2_048L, repository.cacheBytes())
    }

    @Test
    fun `clearCache empties cacheDir and totalBytes drops accordingly`() {
        val repository = repository()
        File(cacheDir, "demo.tmp").writeBytes(ByteArray(4_096))
        assertTrue(repository.cacheBytes() > 0L)

        repository.clearCache()

        assertEquals(0L, repository.cacheBytes())
        assertFalse(File(cacheDir, "demo.tmp").exists())
        assertTrue(cacheDir.exists())
    }

    @Test
    fun `totalBytes is database plus cache`() {
        val repository = repository()
        File(cacheDir, "demo.tmp").writeBytes(ByteArray(1_024))

        assertEquals(repository.databaseBytes() + repository.cacheBytes(), repository.totalBytes())
    }
}
