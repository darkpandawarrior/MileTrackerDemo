package com.mileway.core.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

private val Context.sessionDataStore by preferencesDataStore(name = "user_session")

/**
 * App-wide sign-in session, persisted to DataStore so it survives process death and app restarts.
 *
 * The demo has no real auth, but the *session* (did the user pass the login screen, and how) must
 * persist — otherwise the splash/login theatre replays on every cold start and a guest is bounced
 * back to login, losing their place and any pending deep link. The launcher reads [sessionState]
 * to decide whether to skip straight to the app shell.
 */
class SessionRepository(private val context: Context) {
    private val kindKey = stringPreferencesKey("session_kind")
    private val emailKey = stringPreferencesKey("session_email")
    private val employeeCodeKey = stringPreferencesKey("session_employee_code")
    private val tenantKey = stringPreferencesKey("session_tenant")
    private val signedInAtKey = longPreferencesKey("session_signed_in_at")

    val sessionState: Flow<SessionState> =
        context.sessionDataStore.data.map { prefs ->
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

    /** Persist a credentials sign-in (the demo accepts any non-empty email). */
    suspend fun signInWithCredentials(email: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[kindKey] = SessionKind.CREDENTIALS.name
            prefs[emailKey] = email
            prefs[employeeCodeKey] = deriveEmployeeCode(email)
            prefs[tenantKey] = DEFAULT_SESSION_TENANT
            prefs[signedInAtKey] = Clock.System.now().toEpochMilliseconds()
        }
    }

    /** Persist a guest session so it survives navigation, deep links and process recreation. */
    suspend fun continueAsGuest() {
        context.sessionDataStore.edit { prefs ->
            prefs[kindKey] = SessionKind.GUEST.name
            prefs.remove(emailKey)
            prefs[employeeCodeKey] = deriveEmployeeCode("guest")
            prefs[tenantKey] = DEFAULT_SESSION_TENANT
            prefs[signedInAtKey] = Clock.System.now().toEpochMilliseconds()
        }
    }

    /** Clear the session (sign out) — returns the app to the login screen on next launch. */
    suspend fun signOut() {
        context.sessionDataStore.edit { it.clear() }
    }
}
