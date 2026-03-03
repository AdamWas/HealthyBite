package pl.akp.healthybite.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionDataStore(
    private val dataStore: DataStore<Preferences>
) {

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SessionKeys.IS_LOGGED_IN] ?: false
    }

    val currentUserId: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[SessionKeys.CURRENT_USER_ID]
    }

    suspend fun setLoggedIn(userId: Long) {
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
