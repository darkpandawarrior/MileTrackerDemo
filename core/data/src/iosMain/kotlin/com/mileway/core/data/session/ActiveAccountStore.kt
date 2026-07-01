package com.mileway.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory

/** iOS mirror of the active-account pointer (see the androidMain doc). */
class ActiveAccountStore : ActiveAccountSource {
    private val activeAccountIdKey = stringPreferencesKey("active_account_id")

    private val store: DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { (NSTemporaryDirectory() + "active_account.preferences_pb").toPath() },
        )

    override val activeAccountId: Flow<String?> =
        store.data.map { prefs -> prefs[activeAccountIdKey] }

    override suspend fun setActiveAccountId(accountId: String) {
        store.edit { prefs ->
            prefs[activeAccountIdKey] = accountId
        }
    }
}
