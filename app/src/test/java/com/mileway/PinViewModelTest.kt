package com.mileway

import com.mileway.core.data.session.PIN_GATE_ACCOUNT_ID
import com.mileway.core.data.session.SessionRepository
import com.mileway.core.data.session.sha256Hex
import com.mileway.ui.auth.LOGIN_PIN_LENGTH
import com.mileway.ui.auth.LOGIN_PIN_MAX_ATTEMPTS
import com.mileway.ui.auth.PinUiState
import com.mileway.ui.auth.PinViewModel
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * PLAN_V22 P7.4 — covers [PinViewModel]'s set/verify/lockout state machine backing
 * `SetPinScreen`/`CheckPinScreen`. Mirrors `SwitchAccountViewModelTest`'s (P2.3) shape since both
 * ViewModels share the same `PinHashSource`-backed digit-entry pattern, via [FakePinHashSource].
 */
class PinViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun enterDigits(
        vm: PinViewModel,
        digits: String,
    ) {
        digits.forEach { vm.onDigitEntered(it) }
    }

    @Test
    fun `set PIN flow persists a hash and marks the session once digits and confirmation match`() =
        runTest {
            val hashSource = FakePinHashSource()
            val sessionRepository = mockk<SessionRepository>(relaxed = true)
            val vm = PinViewModel(hashSource, sessionRepository)

            enterDigits(vm, "1234")
            vm.onProceedToConfirm()
            assertTrue(vm.state.value.isConfirmStep)

            enterDigits(vm, "1234")
            vm.confirmSetPin()
            advanceUntilIdle()

            assertTrue(vm.state.value.completed)
            assertEquals(sha256Hex("1234"), hashSource.getPinHash(PIN_GATE_ACCOUNT_ID))
            coVerify { sessionRepository.markPinSet() }
        }

    @Test
    fun `set PIN flow resets the confirm step on a mismatch without completing`() =
        runTest {
            val hashSource = FakePinHashSource()
            val sessionRepository = mockk<SessionRepository>(relaxed = true)
            val vm = PinViewModel(hashSource, sessionRepository)

            enterDigits(vm, "1234")
            vm.onProceedToConfirm()
            enterDigits(vm, "9999")
            vm.confirmSetPin()

            assertFalse(vm.state.value.completed)
            assertEquals("", vm.state.value.confirmDigits)
            assertTrue(vm.state.value.isConfirmStep, "still on the confirm step, not bounced back to the first entry")
            assertEquals("PINs didn't match — try again", vm.state.value.error)
        }

    @Test
    fun `verify succeeds against a previously stored hash`() =
        runTest {
            val hashSource = FakePinHashSource(mapOf(PIN_GATE_ACCOUNT_ID to sha256Hex("4321")))
            val vm = PinViewModel(hashSource, mockk(relaxed = true))

            enterDigits(vm, "4321")
            vm.verify()
            advanceUntilIdle()

            assertTrue(vm.state.value.completed)
            assertEquals(LOGIN_PIN_MAX_ATTEMPTS, vm.state.value.attemptsRemaining)
        }

    @Test
    fun `verify fails when no PIN has ever been set`() =
        runTest {
            val vm = PinViewModel(FakePinHashSource(), mockk(relaxed = true))

            enterDigits(vm, "1234")
            vm.verify()
            advanceUntilIdle()

            assertFalse(vm.state.value.completed)
            assertEquals(LOGIN_PIN_MAX_ATTEMPTS - 1, vm.state.value.attemptsRemaining)
        }

    @Test
    fun `wrong PIN decrements attemptsRemaining and clears entered digits`() =
        runTest {
            val hashSource = FakePinHashSource(mapOf(PIN_GATE_ACCOUNT_ID to sha256Hex("4321")))
            val vm = PinViewModel(hashSource, mockk(relaxed = true))

            enterDigits(vm, "0000")
            vm.verify()
            advanceUntilIdle()

            assertFalse(vm.state.value.completed)
            assertEquals(LOGIN_PIN_MAX_ATTEMPTS - 1, vm.state.value.attemptsRemaining)
            assertEquals("", vm.state.value.digits)
            assertFalse(vm.state.value.isLockedOut)
        }

    @Test
    fun `three wrong attempts latch isLockedOut and further verify calls are ignored`() =
        runTest {
            val hashSource = FakePinHashSource(mapOf(PIN_GATE_ACCOUNT_ID to sha256Hex("4321")))
            val vm = PinViewModel(hashSource, mockk(relaxed = true))

            repeat(LOGIN_PIN_MAX_ATTEMPTS) {
                enterDigits(vm, "0000")
                vm.verify()
                advanceUntilIdle()
            }
            assertTrue(vm.state.value.isLockedOut)
            assertEquals(0, vm.state.value.attemptsRemaining)

            // A correct PIN after lockout must still be rejected — the screen requires a fresh reset().
            enterDigits(vm, "4321")
            vm.verify()
            advanceUntilIdle()
            assertFalse(vm.state.value.completed)
        }

    @Test
    fun `onDigitEntered ignores input once the PIN length is reached`() {
        val vm = PinViewModel(FakePinHashSource(), mockk(relaxed = true))
        enterDigits(vm, "12345")
        assertEquals(LOGIN_PIN_LENGTH, vm.state.value.digits.length)
        assertEquals("1234", vm.state.value.digits)
    }

    @Test
    fun `onBackspace removes the last digit`() {
        val vm = PinViewModel(FakePinHashSource(), mockk(relaxed = true))
        enterDigits(vm, "12")
        vm.onBackspace()
        assertEquals("1", vm.state.value.digits)
    }

    @Test
    fun `reset clears digits, confirm step, attempts, error and lockout`() =
        runTest {
            val hashSource = FakePinHashSource(mapOf(PIN_GATE_ACCOUNT_ID to sha256Hex("4321")))
            val vm = PinViewModel(hashSource, mockk(relaxed = true))
            repeat(LOGIN_PIN_MAX_ATTEMPTS) {
                enterDigits(vm, "0000")
                vm.verify()
                advanceUntilIdle()
            }
            assertTrue(vm.state.value.isLockedOut)

            vm.reset()

            assertEquals(PinUiState(), vm.state.value)
        }
}
