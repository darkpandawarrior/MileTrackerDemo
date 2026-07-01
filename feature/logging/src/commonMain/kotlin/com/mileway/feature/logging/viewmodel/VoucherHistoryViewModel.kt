package com.mileway.feature.logging.viewmodel

import androidx.lifecycle.viewModelScope
import com.mileway.core.ui.mvi.BaseViewModel
import com.mileway.core.ui.mvi.ScreenState
import com.mileway.feature.logging.repository.VoucherHistoryRepository
import com.mileway.feature.logging.repository.VoucherStatus
import com.mileway.feature.logging.ui.model.SubmittedVoucher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** SP.1: voucher-history tabs (the first tab is "All"). */
val VOUCHER_HISTORY_TABS: List<VoucherStatus?> = listOf(null) + VoucherStatus.entries

data class VoucherHistoryUiState(
    val tabIndex: Int = 0,
    val query: String = "",
    val list: ScreenState<List<SubmittedVoucher>> = ScreenState.Loading,
)

sealed interface VoucherHistoryAction {
    data object Refresh : VoucherHistoryAction

    data class SelectTab(val index: Int) : VoucherHistoryAction

    data class SetQuery(val query: String) : VoucherHistoryAction
}

sealed interface VoucherHistoryEffect

/**
 * SP.1/P3.1: reducer for the voucher history surface. Collects the shared, Room-backed
 * [VoucherHistoryRepository] (the same store `feature/tracking`'s Create Voucher writes to),
 * filters by the selected status tab + free-text query, and exposes a [ScreenState] the shared
 * `HistoryListScaffold` renders.
 */
class VoucherHistoryViewModel(
    private val repository: VoucherHistoryRepository,
) : BaseViewModel<VoucherHistoryUiState, VoucherHistoryEffect, VoucherHistoryAction>(VoucherHistoryUiState()) {
    private var reloadJob: Job? = null

    init {
        viewModelScope.launch { repository.seedIfEmpty() }
        reload()
    }

    override fun onAction(action: VoucherHistoryAction) {
        when (action) {
            VoucherHistoryAction.Refresh -> reload()
            is VoucherHistoryAction.SelectTab -> {
                setState { copy(tabIndex = action.index) }
                reload()
            }
            is VoucherHistoryAction.SetQuery -> {
                setState { copy(query = action.query) }
                reload()
            }
        }
    }

    /** Cancels any in-flight collector before starting a new one — tab/query changes replace, not stack. */
    private fun reload() {
        val status = VOUCHER_HISTORY_TABS.getOrNull(currentState.tabIndex)
        reloadJob?.cancel()
        reloadJob =
            viewModelScope.launch {
                repository.observeVouchers(status).collectLatest { rows ->
                    val q = currentState.query.trim()
                    val filtered =
                        rows.filter {
                            q.isEmpty() ||
                                it.id.contains(q, ignoreCase = true) ||
                                it.serviceTag.contains(q, ignoreCase = true) ||
                                it.office.contains(q, ignoreCase = true)
                        }
                    setState { copy(list = ScreenState.Content(filtered)) }
                }
            }
    }
}
