package com.mileway

import com.mileway.core.data.session.SessionRepository
import com.mileway.core.data.session.SessionState
import com.mileway.core.data.settings.DemoSettings
import com.mileway.core.data.settings.DemoSettingsRepository
import com.mileway.core.ui.theme.ThemeController
import com.mileway.feature.profile.repository.FakeProfileRepository
import com.mileway.feature.profile.repository.MockAccountRepository
import com.mileway.feature.profile.viewmodel.ProfileAction
import com.mileway.feature.profile.viewmodel.ProfileEffect
import com.mileway.feature.profile.viewmodel.ProfileViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * PLAN_V22 P2.3 — `ProfileAction.SwitchAccount` gates the switch on
 * `DemoSettingsRepository.biometricGuardEnabled` instead of committing immediately: on, it emits
 * `ProfileEffect.RequestBiometricGate` for the (Android-only) screen to run `BiometricGuard`; off,
 * it sets `pendingSwitchAccountId` so the screen shows `SwitchAccountPinSheet`. Neither path
 * mutates `selectedAccountId`/persists anything until `CommitAccountSwitch` is dispatched — that
 * commit behavior itself is covered by `SwitchAccountReScopeTest`.
 */
class SwitchAccountGateTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(biometricGuardEnabled: Boolean): ProfileViewModel =
        ProfileViewModel(
            FakeProfileRepository(MockAccountRepository(FakeMockAccountDao())),
            ThemeController(),
            FakeActiveAccountSource(seed = "ACC-001"),
            mockk<DemoSettingsRepository> {
                every { settings } returns MutableStateFlow(DemoSettings(biometricGuardEnabled = biometricGuardEnabled))
            },
            mockk<SessionRepository>(relaxed = true) { every { sessionState } returns MutableStateFlow(SessionState()) },
        )

    @Test
    fun `SwitchAccount with biometric guard on emits RequestBiometricGate and does not commit`() =
        runTest {
            val vm = viewModel(biometricGuardEnabled = true)
            advanceUntilIdle()

            vm.onAction(ProfileAction.SwitchAccount("ACC-002"))
            advanceUntilIdle()

            val effect = vm.effect.first()
            assertEquals(ProfileEffect.RequestBiometricGate("ACC-002"), effect)
            assertEquals("ACC-001", vm.uiState.value.selectedAccountId, "must not switch before the biometric gate succeeds")
            assertNull(vm.uiState.value.pendingSwitchAccountId)
        }

    @Test
    fun `SwitchAccount with biometric guard off sets pendingSwitchAccountId instead of switching`() =
        runTest {
            val vm = viewModel(biometricGuardEnabled = false)
            advanceUntilIdle()

            vm.onAction(ProfileAction.SwitchAccount("ACC-002"))
            advanceUntilIdle()

            assertEquals("ACC-002", vm.uiState.value.pendingSwitchAccountId)
            assertEquals("ACC-001", vm.uiState.value.selectedAccountId, "must not switch before the PIN gate succeeds")
        }

    @Test
    fun `CommitAccountSwitch after the PIN gate clears pendingSwitchAccountId and switches`() =
        runTest {
            val vm = viewModel(biometricGuardEnabled = false)
            advanceUntilIdle()

            vm.onAction(ProfileAction.SwitchAccount("ACC-002"))
            advanceUntilIdle()
            vm.onAction(ProfileAction.CommitAccountSwitch("ACC-002"))
            advanceUntilIdle()

            assertEquals("ACC-002", vm.uiState.value.selectedAccountId)
            assertNull(vm.uiState.value.pendingSwitchAccountId)
        }

    @Test
    fun `CancelAccountSwitch clears pendingSwitchAccountId without switching`() =
        runTest {
            val vm = viewModel(biometricGuardEnabled = false)
            advanceUntilIdle()

            vm.onAction(ProfileAction.SwitchAccount("ACC-002"))
            advanceUntilIdle()
            vm.onAction(ProfileAction.CancelAccountSwitch)
            advanceUntilIdle()

            assertNull(vm.uiState.value.pendingSwitchAccountId)
            assertEquals("ACC-001", vm.uiState.value.selectedAccountId)
        }

    @Test
    fun `FallBackToPinGate sets pendingSwitchAccountId when biometric hardware is unavailable`() =
        runTest {
            val vm = viewModel(biometricGuardEnabled = true)
            advanceUntilIdle()

            vm.onAction(ProfileAction.FallBackToPinGate("ACC-002"))
            advanceUntilIdle()

            assertEquals("ACC-002", vm.uiState.value.pendingSwitchAccountId)
        }

    @Test
    fun `SwitchAccount to the already-active account is a no-op`() =
        runTest {
            val vm = viewModel(biometricGuardEnabled = true)
            advanceUntilIdle()

            vm.onAction(ProfileAction.SwitchAccount("ACC-001"))
            advanceUntilIdle()

            assertNull(vm.uiState.value.pendingSwitchAccountId)
        }
}
