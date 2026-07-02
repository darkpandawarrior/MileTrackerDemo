package com.mileway.wear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mileway.feature.tracking.watch.WatchFacade
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * P2.4: the Wear app's single-activity ViewModel (biciradar pattern — one `ViewModel` per
 * `ComponentActivity`, no MVI [com.mileway.core.ui.mvi.BaseViewModel] here since `core:ui` is the
 * phone/iOS Compose Multiplatform theming module `:wear` must never depend on — see
 * [WearAppGraph]'s doc comment). Collects [WatchFacade.observeSnapshot] and maps it through
 * [WearPresentation] into [WearRootUiState] for [WearRootScreen].
 */
class WearViewModel(
    private val watchFacade: WatchFacade,
) : ViewModel() {
    val uiState: StateFlow<WearRootUiState> =
        watchFacade
            .observeSnapshot()
            .map { snapshot -> WearPresentation.toUiState(snapshot) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = WearRootUiState(),
            )

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
