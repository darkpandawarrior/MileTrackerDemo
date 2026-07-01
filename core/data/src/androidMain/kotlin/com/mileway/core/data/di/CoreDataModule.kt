package com.mileway.core.data.di

import com.mileway.core.data.database.MilewayDatabase
import com.mileway.core.data.database.buildMilewayDatabase
import com.mileway.core.data.model.display.InMemorySnapshotPublisher
import com.mileway.core.data.model.display.SnapshotPublisher
import com.mileway.core.data.outbox.RoomSubmitOutbox
import com.mileway.core.data.outbox.SubmitOutbox
import com.mileway.core.data.outbox.TripDraft
import com.mileway.core.data.session.ActiveAccountSource
import com.mileway.core.data.session.ActiveAccountStore
import com.mileway.core.data.session.CurrentTrackDataSource
import com.mileway.core.data.session.CurrentTrackDataStore
import com.mileway.core.data.session.MockAccountSessionCoordinator
import com.mileway.core.data.session.PinHashSource
import com.mileway.core.data.session.PinHashStore
import com.mileway.core.data.session.SessionRepository
import com.mileway.core.data.session.SessionSource
import com.mileway.core.data.settings.AgentSessionStore
import com.mileway.core.data.settings.AgentSessionStoreImpl
import com.mileway.core.data.settings.DemoSettingsRepository
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDataModule =
    module {
        single<MilewayDatabase> { buildMilewayDatabase(androidContext()) }
        single { get<MilewayDatabase>().locationDao() }
        single { get<MilewayDatabase>().savedTrackDao() }
        single { get<MilewayDatabase>().hardwareEventDao() }
        single { get<MilewayDatabase>().logMilesDraftDao() }
        single { get<MilewayDatabase>().logMilesFrequentRouteDao() }
        single { get<MilewayDatabase>().tripAttachmentDao() }
        single { get<MilewayDatabase>().mediaLibraryDao() }
        single { get<MilewayDatabase>().submitDraftDao() }
        single { get<MilewayDatabase>().agentDao() }
        single { get<MilewayDatabase>().draftExpenseDao() }
        single { get<MilewayDatabase>().voucherDao() }
        single { get<MilewayDatabase>().mockAccountDao() }
        single { get<MilewayDatabase>().vehicleDetailsDao() }
        single { get<MilewayDatabase>().passportDetailsDao() }
        single { SessionRepository(androidContext()) }
        single<SessionSource> { get<SessionRepository>() }
        single { CurrentTrackDataStore(androidContext(), get()) }
        single<CurrentTrackDataSource> { get<CurrentTrackDataStore>() }
        single { ActiveAccountStore(androidContext()) }
        single<ActiveAccountSource> { get<ActiveAccountStore>() }
        single { PinHashStore(androidContext()) }
        single<PinHashSource> { get<PinHashStore>() }
        single { DemoSettingsRepository(androidContext()) }
        // P3.4: pause/restore hook run before ProfileViewModel.CommitAccountSwitch flips the
        // active-account pointer.
        single { MockAccountSessionCoordinator(get(), get(), get()) }
        single<AgentSessionStore> { AgentSessionStoreImpl(androidContext()) }
        single { Json { ignoreUnknownKeys = true } }
        single<SubmitOutbox<TripDraft>> { RoomSubmitOutbox(get(), get()) }
        // L.1: in-process snapshot channel for glanceable surfaces (publish + observe).
        single { InMemorySnapshotPublisher() }
        single<SnapshotPublisher> { get<InMemorySnapshotPublisher>() }
    }
