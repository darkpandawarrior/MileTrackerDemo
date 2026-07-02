package com.mileway.core.data.session

import com.mileway.core.data.dao.MockAccountDao
import com.mileway.core.data.model.db.MockAccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class PostLoginFakeMockAccountDao : MockAccountDao {
    val accounts = mutableMapOf<String, MockAccountEntity>()

    override fun observeAll(): Flow<List<MockAccountEntity>> = flowOf(accounts.values.toList())

    override suspend fun count(): Int = accounts.size

    override suspend fun getById(accountId: String): MockAccountEntity? = accounts[accountId]

    override suspend fun upsert(account: MockAccountEntity) {
        accounts[account.accountId] = account
    }

    override suspend fun upsertAll(accounts: List<MockAccountEntity>) {
        accounts.forEach { this.accounts[it.accountId] = it }
    }

    override suspend fun delete(accountId: String) {
        accounts.remove(accountId)
    }

    override suspend fun clearActive() {
        accounts.keys.toList().forEach { key -> accounts[key] = accounts.getValue(key).copy(isActive = false) }
    }

    override suspend fun markActive(accountId: String) {
        accounts[accountId]?.let { accounts[accountId] = it.copy(isActive = true) }
    }
}

/**
 * PLAN_V22 P7.1: [MockPostLoginInitializer.synthesizeProfile] against deterministic in-memory
 * fakes — covers both the "email matches a seeded persona" path (P1.1's `MockAccountDao`) and the
 * "no matching seed yet" fallback, so a fresh install (before `seedIfEmpty()` runs) never fails
 * sign-in for lack of a seeded account.
 */
class MockPostLoginInitializerTest {
    @Test
    fun `synthesizeProfile pulls displayName and organization from the matching seeded account`() =
        runTest {
            val email = "demo@mileway.app"
            val employeeCode = deriveEmployeeCode(email)
            val dao =
                PostLoginFakeMockAccountDao().apply {
                    accounts["ACC-001"] =
                        MockAccountEntity(
                            accountId = "ACC-001",
                            displayName = "Demo User",
                            employeeCode = employeeCode,
                            organization = "Demo Logistics Pvt Ltd",
                            avatarSeed = "ACC-001",
                            isActive = false,
                            lastLoginAtMs = 0L,
                            createdAtMs = 0L,
                        )
                }
            val initializer = MockPostLoginInitializer(dao)

            val profile = initializer.synthesizeProfile(email)

            assertEquals("Demo User", profile.displayName)
            assertEquals(employeeCode, profile.employeeCode)
            assertEquals("Demo Logistics Pvt Ltd", profile.officeName)
        }

    @Test
    fun `synthesizeProfile falls back to a deterministic identity when no seeded account matches`() =
        runTest {
            val initializer = MockPostLoginInitializer(PostLoginFakeMockAccountDao())

            val profile = initializer.synthesizeProfile("stranger@mileway.app")

            assertEquals("stranger", profile.displayName)
            assertEquals(deriveEmployeeCode("stranger@mileway.app"), profile.employeeCode)
            assertEquals("Demo HQ", profile.officeName)
        }

    @Test
    fun `synthesizeProfile is deterministic for the same email across calls`() =
        runTest {
            val initializer = MockPostLoginInitializer(PostLoginFakeMockAccountDao())

            val first = initializer.synthesizeProfile("demo@mileway.app")
            val second = initializer.synthesizeProfile("demo@mileway.app")

            assertEquals(first, second)
        }

    @Test
    fun `synthesizeProfile always populates a non-blank theme color and currency symbol`() =
        runTest {
            val initializer = MockPostLoginInitializer(PostLoginFakeMockAccountDao())

            val profile = initializer.synthesizeProfile("demo@mileway.app")

            assertEquals("#39FF14", profile.themeColorHex)
            assertEquals("₹", profile.currencySymbol)
        }
}
