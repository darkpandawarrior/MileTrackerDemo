package com.mileway.feature.profile.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * PLAN_V22 P1.4: [AccountBadge.initialsFor] backs the Profile tab's persistent active-account
 * badge — it must derive a stable two-letter (or fewer) initials string from any active persona's
 * display name, including single-word names and the blank edge case.
 */
class AccountBadgeTest {
    @Test
    fun `two word name yields both first letters uppercased`() {
        assertEquals("JD", AccountBadge.initialsFor("Jordan Diaz"))
    }

    @Test
    fun `single word name yields one letter`() {
        assertEquals("A", AccountBadge.initialsFor("Ananya"))
    }

    @Test
    fun `three or more word name still yields only two letters`() {
        assertEquals("RK", AccountBadge.initialsFor("Ravi Kiran Mehta"))
    }

    @Test
    fun `extra whitespace between words is ignored`() {
        assertEquals("JD", AccountBadge.initialsFor("  Jordan   Diaz  "))
    }

    @Test
    fun `blank name falls back to a placeholder`() {
        assertEquals("?", AccountBadge.initialsFor(""))
        assertEquals("?", AccountBadge.initialsFor("   "))
    }
}
