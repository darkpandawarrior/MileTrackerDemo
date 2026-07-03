package com.mileway

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mileway.core.data.settings.StorageRepository
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * P6.6: [StorageRepository]'s byte-count readout + clear-cache action, backing Preferences'
 * Storage tile/sheet — real [android.content.Context.cacheDir] I/O via Robolectric, not a mock.
 *
 * Assertions are delta-based (measure a baseline, then assert the change) rather than asserting
 * absolute cache totals: cacheDir is shared process-wide, so a sibling test in the full suite can
 * leave stray files there. The delta is what StorageRepository is actually responsible for.
 */
@RunWith(AndroidJUnit4::class)
class StorageRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val repository = StorageRepository(context)

    @Test
    fun `cacheBytes reflects files written under cacheDir`() {
        context.cacheDir.mkdirs()
        val baseline = repository.cacheBytes()

        val file = java.io.File(context.cacheDir, "cacheBytes-demo.tmp")
        file.writeBytes(ByteArray(2_048))
        try {
            assertEquals(baseline + 2_048L, repository.cacheBytes())
        } finally {
            file.delete()
        }
    }

    @Test
    fun `clearCache empties cacheDir and totalBytes drops accordingly`() {
        context.cacheDir.mkdirs()
        java.io.File(context.cacheDir, "clearCache-demo.tmp").writeBytes(ByteArray(4_096))
        assertTrue(repository.cacheBytes() >= 4_096L)

        repository.clearCache()

        assertEquals(0L, repository.cacheBytes())
        assertFalse(java.io.File(context.cacheDir, "clearCache-demo.tmp").exists())
        assertTrue(context.cacheDir.exists())
    }

    @Test
    fun `totalBytes is database plus cache`() {
        context.cacheDir.mkdirs()
        val file = java.io.File(context.cacheDir, "totalBytes-demo.tmp")
        file.writeBytes(ByteArray(1_024))
        try {
            assertEquals(repository.databaseBytes() + repository.cacheBytes(), repository.totalBytes())
        } finally {
            file.delete()
        }
    }
}
