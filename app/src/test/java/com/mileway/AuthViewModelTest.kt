package com.mileway

import app.cash.turbine.test
import com.mileway.ui.auth.AuthViewModel
import com.mileway.ui.auth.MilewayAuthState
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * P7.2: behavioural tests for [AuthViewModel], the reducer behind [MilewayAuthState] driving
 * [com.mileway.ui.auth.LoginScreen]'s staged sign-in loading UX.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `starts idle`() = runTest {
        val vm = AuthViewModel()
        assertEquals(MilewayAuthState.Idle, vm.state.value)
    }

    @Test
    fun `beginSignIn steps through every named stage before reporting success`() = runTest {
        val vm = AuthViewModel()

        vm.state.test {
            assertEquals(MilewayAuthState.Idle, awaitItem())

            vm.beginSignIn()

            val step1 = awaitItem()
            assertIs<MilewayAuthState.Loading>(step1)
            assertEquals(1, step1.step)
            assertEquals(3, step1.totalSteps)
            assertTrue(step1.label.isNotBlank())

            val step2 = awaitItem()
            assertIs<MilewayAuthState.Loading>(step2)
            assertEquals(2, step2.step)
            assertTrue(step2.label != step1.label)

            val step3 = awaitItem()
            assertIs<MilewayAuthState.Loading>(step3)
            assertEquals(3, step3.step)

            assertEquals(MilewayAuthState.Success, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a second beginSignIn call while loading is a no-op`() = runTest {
        val vm = AuthViewModel()

        vm.beginSignIn()
        advanceUntilIdle()
        assertEquals(MilewayAuthState.Success, vm.state.value)

        // Reset back to idle, start a fresh run, then try to re-trigger mid-sequence.
        vm.reset()
        vm.beginSignIn()
        runCurrent() // advance just enough to reach the first Loading state (before its delay)
        val midSequenceState = vm.state.value
        assertIs<MilewayAuthState.Loading>(midSequenceState)

        vm.beginSignIn() // no-op: already Loading
        assertEquals(midSequenceState, vm.state.value)

        advanceUntilIdle()
        assertEquals(MilewayAuthState.Success, vm.state.value)
    }

    @Test
    fun `reset cancels an in-flight sequence and returns to idle`() = runTest {
        val vm = AuthViewModel()

        vm.beginSignIn()
        runCurrent() // advance just enough to reach the first Loading state (before its delay)
        assertIs<MilewayAuthState.Loading>(vm.state.value)

        vm.reset()
        assertEquals(MilewayAuthState.Idle, vm.state.value)

        // Letting time pass after reset must not resurrect the cancelled sequence.
        advanceUntilIdle()
        assertEquals(MilewayAuthState.Idle, vm.state.value)
    }
}
