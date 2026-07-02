package com.mileway

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mileway.core.data.settings.StorageRepository
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * P6.6: [StorageRepository]'s byte-count readout + clear-cache action, backing Preferences'
 * Storage tile/sheet — real [android.content.Context.cacheDir] I/O via Robolectric, not a mock.
 */
@RunWith(AndroidJUnit4::class)
class StorageRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val repository = StorageRepository(context)

    @Test
    fun `cacheBytes reflects files written under cacheDir`() {
        context.cacheDir.deleteRecursively()
        context.cacheDir.mkdirs()
        assertEquals(0L, repository.cacheBytes())

        val file = java.io.File(context.cacheDir, "demo.tmp")
        file.writeBytes(ByteArray(2_048))

        assertEquals(2_048L, repository.cacheBytes())
    }

    @Test
    fun `clearCache empties cacheDir and totalBytes drops accordingly`() {
        context.cacheDir.deleteRecursively()
        context.cacheDir.mkdirs()
        java.io.File(context.cacheDir, "demo.tmp").writeBytes(ByteArray(4_096))
        assertTrue(repository.cacheBytes() > 0L)

        repository.clearCache()

        assertEquals(0L, repository.cacheBytes())
        assertTrue(context.cacheDir.exists())
    }

    @Test
    fun `totalBytes is database plus cache`() {
        context.cacheDir.deleteRecursively()
        context.cacheDir.mkdirs()
        java.io.File(context.cacheDir, "demo.tmp").writeBytes(ByteArray(1_024))

        assertEquals(repository.databaseBytes() + repository.cacheBytes(), repository.totalBytes())
    }
}
