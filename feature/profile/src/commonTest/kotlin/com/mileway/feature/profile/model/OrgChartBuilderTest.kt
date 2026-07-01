package com.mileway.feature.profile.model

import com.mileway.core.network.model.DemoAccount
import com.mileway.core.network.model.EmployeeProfile
import com.mileway.core.network.model.EmployeeSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * P6.2: [OrgChartBuilder.build] produces a real (if small) chart out of the seeded personas
 * (P1.1) plus [EmployeeProfile.manager]'s link — replacing the prior no-op "Reporting Manager"
 * tile tap.
 */
class OrgChartBuilderTest {
    private val accounts =
        listOf(
            DemoAccount(id = "ACC-001", displayName = "Demo User", employeeCode = "EMP001", organization = "Demo Logistics Pvt Ltd"),
            DemoAccount(id = "ACC-002", displayName = "Demo User (Sandbox)", employeeCode = "EMP001-SBX", organization = "Demo Sandbox Workspace"),
            DemoAccount(id = "ACC-003", displayName = "QA Tester", employeeCode = "QA042", organization = "Demo QA Workspace"),
        )

    private val profile =
        EmployeeProfile(
            name = "Demo User",
            email = "demo@mileway.app",
            employeeCode = "EMP001",
            role = "Field Sales Executive",
            organization = "Demo Logistics Pvt Ltd",
            manager = EmployeeSummary(id = "ACC-002", name = "Asha Verma", code = "EMP001-SBX"),
        )

    @Test
    fun `chart has a root, a linked manager node, and the current user as a report`() {
        val root = OrgChartBuilder.build(profile, accounts, currentAccountId = "ACC-001")

        assertEquals(1, root.reports.size, "expected exactly one manager node under the root")
        val manager = root.reports.single()
        assertEquals("Asha Verma", manager.name)
        assertEquals("ACC-002", manager.id)

        val currentUserNode = manager.reports.single { it.isCurrentUser }
        assertEquals("Demo User", currentUserNode.name)
        assertTrue(manager.reports.any { it.id == "ACC-003" }, "the other seeded persona should also report to the same manager")
    }

    @Test
    fun `three levels are produced -- root, manager, reports`() {
        val root = OrgChartBuilder.build(profile, accounts, currentAccountId = "ACC-001")

        assertEquals("org-root", root.id)
        val manager = root.reports.single()
        assertTrue(manager.reports.isNotEmpty(), "manager node must have at least one report")
    }

    @Test
    fun `an unlinked manager (no matching seeded account) still renders a manager node by name`() {
        val unlinkedProfile = profile.copy(manager = EmployeeSummary(id = "ACC-999", name = "Someone Else"))

        val root = OrgChartBuilder.build(unlinkedProfile, accounts, currentAccountId = "ACC-001")

        val manager = root.reports.single()
        assertEquals("Someone Else", manager.name)
        assertEquals(3, manager.reports.size, "all seeded accounts (none matched the unlinked manager) become peers/reports")
    }
}
