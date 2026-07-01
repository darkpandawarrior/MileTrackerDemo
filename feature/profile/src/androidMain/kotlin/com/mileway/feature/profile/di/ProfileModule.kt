package com.mileway.feature.profile.di

import com.mileway.feature.profile.repository.ActiveSessionsRepository
import com.mileway.feature.profile.repository.AdvanceRepository
import com.mileway.feature.profile.repository.DelegationRepository
import com.mileway.feature.profile.repository.FakeProfileRepository
import com.mileway.feature.profile.repository.MockAccountRepository
import com.mileway.feature.profile.repository.PassportDetailsRepository
import com.mileway.feature.profile.repository.ProfileRepository
import com.mileway.feature.profile.repository.VehicleDetailsRepository
import com.mileway.feature.profile.viewmodel.ActiveSessionsViewModel
import com.mileway.feature.profile.viewmodel.AdvanceViewModel
import com.mileway.feature.profile.viewmodel.DelegationViewModel
import com.mileway.feature.profile.viewmodel.DemoSettingsViewModel
import com.mileway.feature.profile.viewmodel.PersonalDetailsViewModel
import com.mileway.feature.profile.viewmodel.ProfileViewModel
import com.mileway.feature.profile.viewmodel.SwitchAccountViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule =
    module {
        single { MockAccountRepository(get()) }
        single<ProfileRepository> { FakeProfileRepository(get()) }
        single { AdvanceRepository() }
        // P6.2: Vehicle/Passport tiles' Room-backed repositories.
        single { VehicleDetailsRepository(get()) }
        single { PassportDetailsRepository(get()) }
        // P6.3: approval-delegation Room-backed repository (see DelegationScreen).
        single { DelegationRepository(get()) }
        // P6.4: Active Sessions' Room-backed repository (see ActiveSessionsScreen).
        single { ActiveSessionsRepository(get()) }
        viewModelOf(::ProfileViewModel)
        viewModel { AdvanceViewModel(get()) }
        viewModelOf(::DemoSettingsViewModel)
        viewModelOf(::SwitchAccountViewModel)
        viewModelOf(::PersonalDetailsViewModel)
        viewModelOf(::DelegationViewModel)
        viewModelOf(::ActiveSessionsViewModel)
    }
