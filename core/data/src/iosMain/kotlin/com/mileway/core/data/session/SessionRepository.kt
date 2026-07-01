package com.mileway.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory
import kotlin.time.Clock

/** iOS mirror of the app-wide sign-in session (see the androidMain doc). */
class SessionRepository : SessionSource {
    private val kindKey = stringPreferencesKey("session_kind")
    private val emailKey = stringPreferencesKey("session_email")
    private val employeeCodeKey = stringPreferencesKey("session_employee_code")
    private val tenantKey = stringPreferencesKey("session_tenant")
    private val signedInAtKey = longPreferencesKey("session_signed_in_at")

    private val store: DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { (NSTemporaryDirectory() + "user_session.preferences_pb").toPath() },
        )

    override val sessionState: Flow<SessionState> =
        store.data.map { prefs ->
            val kind =
                prefs[kindKey]?.let { stored ->
                    SessionKind.entries.firstOrNull { it.name == stored }
                } ?: SessionKind.NONE
            SessionState(
                kind = kind,
                email = prefs[emailKey],
                employeeCode = prefs[employeeCodeKey],
                tenant = prefs[tenantKey] ?: DEFAULT_SESSION_TENANT,
                signedInAtMillis = prefs[signedInAtKey],
            )
        }

    suspend fun signInWithCredentials(email: String) {
        store.edit { prefs ->
            prefs[kindKey] = SessionKind.CREDENTIALS.name
            prefs[emailKey] = email
            prefs[employeeCodeKey] = deriveEmployeeCode(email)
            prefs[tenantKey] = DEFAULT_SESSION_TENANT
            prefs[signedInAtKey] = Clock.System.now().toEpochMilliseconds()
        }
    }

    suspend fun continueAsGuest() {
        store.edit { prefs ->
            prefs[kindKey] = SessionKind.GUEST.name
            prefs.remove(emailKey)
            prefs[employeeCodeKey] = deriveEmployeeCode("guest")
            prefs[tenantKey] = DEFAULT_SESSION_TENANT
            prefs[signedInAtKey] = Clock.System.now().toEpochMilliseconds()
        }
    }

    suspend fun signOut() {
        store.edit { it.clear() }
    }
}
