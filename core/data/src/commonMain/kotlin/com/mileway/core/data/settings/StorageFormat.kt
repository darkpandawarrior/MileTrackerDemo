package com.mileway.core.data.settings

/**
 * PLAN_V22 P6.6: KMP-safe (no JVM-only `String.format`) formatter for
 * [com.mileway.core.data.settings.StorageRepository]'s byte counts, shown on Preferences' Storage
 * tile/sheet subtitle.
 */
fun formatStorageBytes(bytes: Long): String =
    when {
        bytes >= 1_048_576L -> "${oneDecimal(bytes, 1_048_576.0)} MB"
        bytes >= 1_024L -> "${oneDecimal(bytes, 1_024.0)} KB"
        else -> "$bytes B"
    }

/** Rounds [bytes] / [divisor] to one decimal place (half-up) without `String.format`. */
private fun oneDecimal(
    bytes: Long,
    divisor: Double,
): String {
    val tenths = kotlin.math.round(bytes / divisor * 10).toLong()
    return "${tenths / 10}.${tenths % 10}"
}
