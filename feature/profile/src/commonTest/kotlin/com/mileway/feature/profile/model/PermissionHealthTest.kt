package com.mileway.feature.profile.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * PLAN_V22 P6.7: [computePermissionHealth] replaces Settings' previous hardcoded "90%" ring —
 * these cases cover all-granted, all-denied, and a representative mixed required/recommended
 * state, plus the empty-list edge case (no permissions declared at all).
 */
class PermissionHealthTest {
    private fun entry(
        name: String,
        isRequired: Boolean,
        isGranted: Boolean,
    ) = PermissionHealthEntry(name = name, isRequired = isRequired, isGranted = isGranted)

    @Test
    fun `all permissions granted scores 100 percent`() {
        val entries =
            listOf(
                entry("Location", isRequired = true, isGranted = true),
                entry("Camera", isRequired = true, isGranted = true),
                entry("Bluetooth", isRequired = false, isGranted = true),
            )

        val summary = computePermissionHealth(entries)

        assertEquals(100, summary.healthScorePercent)
        assertEquals(2, summary.requiredGranted)
        assertEquals(2, summary.requiredTotal)
        assertEquals(1, summary.recommendedGranted)
        assertEquals(1, summary.recommendedTotal)
    }

    @Test
    fun `all permissions denied scores 0 percent`() {
        val entries =
            listOf(
                entry("Location", isRequired = true, isGranted = false),
                entry("Camera", isRequired = true, isGranted = false),
            )

        val summary = computePermissionHealth(entries)

        assertEquals(0, summary.healthScorePercent)
        assertEquals(0, summary.requiredGranted)
        assertEquals(2, summary.requiredTotal)
    }

    @Test
    fun `a mixed required-recommended state computes the real granted-over-total ratio`() {
        // 3 of 4 total granted = 75%, matching a real device state rather than a hardcoded value.
        val entries =
            listOf(
                entry("Location", isRequired = true, isGranted = true),
                entry("Camera", isRequired = true, isGranted = true),
                entry("Storage", isRequired = true, isGranted = false),
                entry("Bluetooth", isRequired = false, isGranted = true),
            )

        val summary = computePermissionHealth(entries)

        assertEquals(75, summary.healthScorePercent)
        assertEquals(2, summary.requiredGranted)
        assertEquals(3, summary.requiredTotal)
        assertEquals(1, summary.recommendedGranted)
        assertEquals(1, summary.recommendedTotal)
    }

    @Test
    fun `an empty permission list scores 100 percent instead of dividing by zero`() {
        val summary = computePermissionHealth(emptyList())

        assertEquals(100, summary.healthScorePercent)
        assertEquals(0, summary.requiredTotal)
        assertEquals(0, summary.recommendedTotal)
    }
}
