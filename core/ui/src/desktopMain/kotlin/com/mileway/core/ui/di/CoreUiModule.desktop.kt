package com.mileway.core.ui.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val coreUiPlatformModule: Module =
    module {
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    File(System.getProperty("user.home"), ".mileway/theme_prefs.preferences_pb")
                        .also { it.parentFile.mkdirs() }
                        .path
                        .toPath()
                },
            )
        }
    }
