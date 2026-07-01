package com.mileway.core.data.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * PLAN_V22 P3.1: pure `commonMain` coverage for the widened [SessionState] identity block —
 * [deriveEmployeeCode]'s deterministic derivation and the [DEFAULT_SESSION_TENANT] default that
 * [SessionRepository] stamps on sign-in. The DataStore-backed persistence itself is exercised via
 * instrumentation/Robolectric (see `app/src/test/java/com/mileway/SessionStateTest.kt`); this test
 * locks the pure logic those platform tests depend on.
 */
class SessionStateTest {
    @Test
    fun `deriveEmployeeCode is deterministic for the same email`() {
        assertEquals(deriveEmployeeCode("demo@mileway.app"), deriveEmployeeCode("demo@mileway.app"))
    }

    @Test
    fun `deriveEmployeeCode differs for different emails`() {
        val a = deriveEmployeeCode("alice@mileway.app")
        val b = deriveEmployeeCode("bob@mileway.app")
        assertTrue(a != b, "distinct emails should (almost always) synthesize distinct codes")
    }

    @Test
    fun `deriveEmployeeCode always carries the EMP- prefix`() {
        assertTrue(deriveEmployeeCode("demo@mileway.app").startsWith("EMP-"))
        assertTrue(deriveEmployeeCode("").startsWith("EMP-"))
    }

    @Test
    fun `default tenant is DEMO-TENANT`() {
        assertEquals("DEMO-TENANT", DEFAULT_SESSION_TENANT)
        assertEquals(DEFAULT_SESSION_TENANT, SessionState().tenant)
    }

    @Test
    fun `widened fields default to null-ish identity for a fresh state`() {
        val state = SessionState()
        assertEquals(null, state.employeeCode)
        assertEquals(null, state.signedInAtMillis)
        assertEquals(DEFAULT_SESSION_TENANT, state.tenant)
    }

    @Test
    fun `existing 2-field behavior is unchanged for kind and email`() {
        val state = SessionState(kind = SessionKind.CREDENTIALS, email = "demo@mileway.app")
        assertTrue(state.isSignedIn)
        assertEquals("demo@mileway.app", state.email)
    }
}
