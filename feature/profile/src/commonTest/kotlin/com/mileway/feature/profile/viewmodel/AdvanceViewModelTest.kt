package com.mileway.feature.profile.viewmodel

import com.mileway.core.ui.mvi.ScreenState
import com.mileway.core.ui.mvi.dataOrNull
import com.mileway.feature.profile.repository.AdvanceRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AdvanceViewModelTest {
    private fun buildVm() = AdvanceViewModel(AdvanceRepository())

    @Test
    fun `detail state starts as Loading before any LoadDetail action`() {
        val vm = buildVm()
        assertIs<ScreenState.Loading>(vm.state.value.detail)
    }

    @Test
    fun `LoadDetail with a known id populates detail with that record`() {
        val vm = buildVm()
        vm.onAction(AdvanceAction.LoadDetail("ADV-001"))
        val detail = vm.state.value.detail
        assertIs<ScreenState.Content<*>>(detail)
        assertEquals("ADV-001", detail.dataOrNull?.id)
    }

    @Test
    fun `LoadDetail with an unknown id yields Empty`() {
        val vm = buildVm()
        vm.onAction(AdvanceAction.LoadDetail("ADV-DOES-NOT-EXIST"))
        assertIs<ScreenState.Empty>(vm.state.value.detail)
    }
}
