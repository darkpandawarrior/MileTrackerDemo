package com.mileway.core.data.settings

import kotlin.test.Test
import kotlin.test.assertEquals

/** P6.6: [formatStorageBytes]'s byte-range formatting, used by Preferences' Storage tile/sheet. */
class StorageFormatTest {
    @Test
    fun `bytes below 1024 render as whole-number bytes`() {
        assertEquals("0 B", formatStorageBytes(0L))
        assertEquals("512 B", formatStorageBytes(512L))
        assertEquals("1023 B", formatStorageBytes(1023L))
    }

    @Test
    fun `kilobyte range renders with one decimal place`() {
        assertEquals("1.0 KB", formatStorageBytes(1_024L))
        assertEquals("1.5 KB", formatStorageBytes(1_536L))
    }

    @Test
    fun `megabyte range renders with one decimal place`() {
        assertEquals("1.0 MB", formatStorageBytes(1_048_576L))
        assertEquals("2.5 MB", formatStorageBytes((1_048_576L * 2.5).toLong()))
    }
}
