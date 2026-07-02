package com.mileway.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory
import kotlin.time.Clock

/**
 * iOS mirror of the app-wide sign-in session (see the androidMain doc).
 *
 * PLAN_V22 P7.1: [signInWithCredentials] runs [MockPostLoginInitializer] to synthesize the same
 * post-login bootstrap block androidMain persists — still no network call.
 */
class SessionRepository(
    private val postLoginInitializer: MockPostLoginInitializer,
) : SessionSource {
    private val kindKey = stringPreferencesKey("session_kind")
    private val emailKey = stringPreferencesKey("session_email")
    private val employeeCodeKey = stringPreferencesKey("session_employee_code")
    private val tenantKey = stringPreferencesKey("session_tenant")
    private val signedInAtKey = longPreferencesKey("session_signed_in_at")
    private val displayNameKey = stringPreferencesKey("session_display_name")
    private val officeNameKey = stringPreferencesKey("session_office_name")
    private val themeColorHexKey = stringPreferencesKey("session_theme_color_hex")
    private val currencySymbolKey = stringPreferencesKey("session_currency_symbol")
    private val firstLoginPendingKey = booleanPreferencesKey("session_first_login_pending")

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
                displayName = prefs[displayNameKey],
                officeName = prefs[officeNameKey],
                themeColorHex = prefs[themeColorHexKey],
                currencySymbol = prefs[currencySymbolKey],
                isFirstLoginPending = prefs[firstLoginPendingKey] ?: false,
            )
        }

    suspend fun signInWithCredentials(email: String) {
        val profile = postLoginInitializer.synthesizeProfile(email)
        store.edit { prefs ->
            prefs[kindKey] = SessionKind.CREDENTIALS.name
            prefs[emailKey] = email
            prefs[employeeCodeKey] = profile.employeeCode
            prefs[tenantKey] = DEFAULT_SESSION_TENANT
            prefs[signedInAtKey] = Clock.System.now().toEpochMilliseconds()
            prefs[displayNameKey] = profile.displayName
            prefs[officeNameKey] = profile.officeName
            prefs[themeColorHexKey] = profile.themeColorHex
            prefs[currencySymbolKey] = profile.currencySymbol
            prefs[firstLoginPendingKey] = true
        }
    }

    suspend fun continueAsGuest() {
        store.edit { prefs ->
            prefs[kindKey] = SessionKind.GUEST.name
            prefs.remove(emailKey)
            prefs[employeeCodeKey] = deriveEmployeeCode("guest")
            prefs[tenantKey] = DEFAULT_SESSION_TENANT
            prefs[signedInAtKey] = Clock.System.now().toEpochMilliseconds()
            prefs.remove(displayNameKey)
            prefs.remove(officeNameKey)
            prefs.remove(themeColorHexKey)
            prefs.remove(currencySymbolKey)
            prefs.remove(firstLoginPendingKey)
        }
    }

    /** Clears [SessionState.isFirstLoginPending] once the welcome banner has shown. */
    suspend fun clearFirstLoginPending() {
        store.edit { prefs ->
            prefs[firstLoginPendingKey] = false
        }
    }

    suspend fun signOut() {
        store.edit { it.clear() }
    }
}
