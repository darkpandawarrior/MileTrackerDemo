package com.mileway.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mileway.feature.profile.model.SyncMetrics
import com.mileway.feature.profile.repository.SyncDiagnosticsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** PLAN_V22 P6.7: thin passthrough VM for Settings' `SyncDiagnosticsCard`. */
class SyncDiagnosticsViewModel(private val repository: SyncDiagnosticsRepository) : ViewModel() {
    val metrics: StateFlow<SyncMetrics> = repository.metrics

    fun forceSync() {
        viewModelScope.launch { repository.forceSync() }
    }
}
