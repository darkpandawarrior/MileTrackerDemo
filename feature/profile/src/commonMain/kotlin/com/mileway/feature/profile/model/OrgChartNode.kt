package com.mileway.feature.profile.model

import com.mileway.core.network.model.DemoAccount
import com.mileway.core.network.model.EmployeeProfile

/** One node in the small demo org chart pushed from the "Reporting Manager" tile. */
data class OrgChartNode(
    val id: String,
    val name: String,
    val role: String,
    val isCurrentUser: Boolean = false,
    val reports: List<OrgChartNode> = emptyList(),
)

/**
 * P6.2: builds a small (3-4 level), fully local org chart out of the seeded personas
 * ([MockAccountRepository][com.mileway.feature.profile.repository.MockAccountRepository]'s demo
 * accounts, P1.1) plus [profile]'s own [EmployeeProfile.manager] link — no real org-chart backend,
 * matching the plan's explicit scope note (guest management / FinOS tiles are out of scope, but a
 * real-if-small chart from already-seeded data is in scope).
 *
 * Shape: a synthetic top-of-chart root ("Demo Logistics Pvt Ltd" leadership) → [profile.manager]
 * (when linked to a real seeded persona) → the signed-in user, as a sibling of the other seeded
 * personas that share the same manager. Personas with no manager link of their own render directly
 * under the root.
 */
object OrgChartBuilder {
    fun build(
        profile: EmployeeProfile,
        accounts: List<DemoAccount>,
        currentAccountId: String,
    ): OrgChartNode {
        val manager = profile.manager
        val managerAccount = manager?.let { m -> accounts.find { it.id == m.id } }

        val peers =
            accounts
                .filter { it.id != managerAccount?.id }
                .map { account ->
                    OrgChartNode(
                        id = account.id,
                        name = if (account.id == currentAccountId) profile.name.ifBlank { account.displayName } else account.displayName,
                        role = if (account.id == currentAccountId) profile.role.ifBlank { "Team Member" } else "Team Member",
                        isCurrentUser = account.id == currentAccountId,
                    )
                }

        val managerNode =
            OrgChartNode(
                id = managerAccount?.id ?: "manager-unlinked",
                name = manager?.name ?: managerAccount?.displayName ?: "Unassigned",
                role = "Reporting Manager",
                reports = peers,
            )

        return OrgChartNode(
            id = "org-root",
            name = profile.organization.ifBlank { "Leadership" },
            role = "Organization",
            reports = listOf(managerNode),
        )
    }
}
