package com.mileway.feature.profile.model

import com.mileway.core.network.model.EmployeeProfile
import com.mileway.core.network.model.EmployeeSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * P6.1: [ProfileFieldCompletion.derive] replaces the old static category-level `done/total` pair
 * with a real per-field derivation straight off [EmployeeProfile]'s blank/non-blank values —
 * these cases cover a fully-complete profile, a fully-blank one, and representative
 * partially-filled states (required vs optional fields missing).
 */
class ProfileFieldCompletionTest {
    private val completeProfile =
        EmployeeProfile(
            name = "Demo User",
            email = "demo@mileway.app",
            employeeCode = "EMP001",
            phone = "+91 98765 43210",
            gender = "Male",
            role = "Field Sales Executive",
            organization = "Demo Logistics Pvt Ltd",
            manager = EmployeeSummary(id = "ACC-002", name = "Asha Verma"),
            homeLocation = "Baner, Pune",
        )

    @Test
    fun `a fully complete profile has 100 percent and no missing fields`() {
        val result = ProfileFieldCompletion.derive(completeProfile)

        assertEquals(100, result.percent)
        assertEquals(result.totalCount, result.completedCount)
        assertTrue(result.missingFields.isEmpty())
    }

    @Test
    fun `a fully blank profile has 0 percent and every field missing`() {
        val blank = EmployeeProfile(name = "", email = "", employeeCode = "")

        val result = ProfileFieldCompletion.derive(blank)

        assertEquals(0, result.percent)
        assertEquals(0, result.completedCount)
        assertEquals(result.totalCount, result.missingFields.size)
    }

    @Test
    fun `a blank required field surfaces as a missing field with a ProfileDetails route`() {
        val missingManager = completeProfile.copy(manager = null)

        val result = ProfileFieldCompletion.derive(missingManager)

        val spec = result.missingFields.single()
        assertEquals("d_manager", spec.fieldId)
        assertEquals(ProfileRoute.ProfileDetails("d_manager"), spec.route)
        assertTrue(result.percent < 100)
    }

    @Test
    fun `required fields sort ahead of optional fields in the missing list`() {
        // gender (optional) declared before organization/manager (required) in field order,
        // but required fields must still come first in the derived, sorted list.
        val profile = completeProfile.copy(gender = "", organization = "", manager = null)

        val result = ProfileFieldCompletion.derive(profile)

        assertEquals(listOf("d_org", "d_manager", "d_gender"), result.missingFields.map { it.fieldId })
        assertEquals(listOf(0, 1, 2), result.missingFields.map { it.priority })
    }

    @Test
    fun `missing an optional field only reduces completion by one field`() {
        val missingHome = completeProfile.copy(homeLocation = "")

        val result = ProfileFieldCompletion.derive(missingHome)

        assertEquals(1, result.missingFields.size)
        assertEquals("d_home", result.missingFields.single().fieldId)
        assertEquals(result.totalCount - 1, result.completedCount)
    }
}
