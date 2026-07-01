package com.mileway.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory

/** iOS mirror of the per-account PIN hash store (see the androidMain doc). */
class PinHashStore : PinHashSource {
    private val store: DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { (NSTemporaryDirectory() + "pin_hash.preferences_pb").toPath() },
        )

    override suspend fun getPinHash(accountId: String): String? =
        store.data
            .map { prefs -> prefs[keyFor(accountId)] }
            .firstOrNull()

    override suspend fun setPinHash(
        accountId: String,
        pinHash: String,
    ) {
        store.edit { prefs -> prefs[keyFor(accountId)] = pinHash }
    }

    private fun keyFor(accountId: String) = stringPreferencesKey("pin_hash_$accountId")
}
