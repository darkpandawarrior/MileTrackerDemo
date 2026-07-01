package com.mileway.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mileway.feature.profile.model.PassportDetails
import com.mileway.feature.profile.model.VehicleDetails
import com.mileway.feature.profile.repository.PassportDetailsRepository
import com.mileway.feature.profile.repository.VehicleDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PLAN_V22 P6.2: state for the Vehicle/Passport add-edit bottom sheets pushed from
 * [ProfileDetailsScreen][com.mileway.feature.profile.ui.screens.ProfileDetailsScreen]. Both live
 * on one VM since neither sheet is shown at the same time as the other and the shape (an
 * optional persisted record + a save action) is identical.
 */
data class PersonalDetailsUiState(
    val vehicle: VehicleDetails? = null,
    val passport: PassportDetails? = null,
)

class PersonalDetailsViewModel(
    private val vehicleRepository: VehicleDetailsRepository,
    private val passportRepository: PassportDetailsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(PersonalDetailsUiState())
    val state: StateFlow<PersonalDetailsUiState> = _state.asStateFlow()

    init {
        vehicleRepository.observe().onEach { vehicle -> _state.update { it.copy(vehicle = vehicle) } }.launchIn(viewModelScope)
        passportRepository.observe().onEach { passport -> _state.update { it.copy(passport = passport) } }.launchIn(viewModelScope)
    }

    fun saveVehicle(details: VehicleDetails) {
        viewModelScope.launch { vehicleRepository.save(details) }
    }

    fun savePassport(details: PassportDetails) {
        viewModelScope.launch { passportRepository.save(details) }
    }
}
