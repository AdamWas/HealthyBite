package pl.akp.healthybite.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * New file name so we do not read legacy `session` preferences that may contain the same key names
 * stored under a different protobuf type (e.g. Long vs String), which causes
 * `ClassCastException: Long cannot be cast to String` in [Preferences.get].
 */
val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "healthybite_session_v2")

private const val SESSION_TAG = "HealthyBiteSession"

class SessionDataStore(
    private val dataStore: DataStore<Preferences>
) {

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        runCatching { prefs[SessionKeys.IS_LOGGED_IN] ?: false }
            .onFailure { e ->
                Log.e(SESSION_TAG, "isLoggedIn read failed (type mismatch in prefs?)", e)
            }
            .getOrDefault(false)
    }

    val currentUserId: Flow<String?> = dataStore.data.map { prefs ->
        runCatching { prefs[SessionKeys.CURRENT_USER_ID] }
            .onFailure { e ->
                Log.e(
                    SESSION_TAG,
                    "currentUserId read failed — key=${SessionKeys.CURRENT_USER_ID.name}; " +
                        "if this is ClassCastException, old DataStore file had wrong value type",
                    e
                )
            }
            .getOrNull()
    }

    suspend fun setLoggedIn(userId: String) {
        dataStore.edit { prefs ->
            prefs[SessionKeys.IS_LOGGED_IN] = true
            prefs[SessionKeys.CURRENT_USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs[SessionKeys.IS_LOGGED_IN] = false
            prefs.remove(SessionKeys.CURRENT_USER_ID)
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
