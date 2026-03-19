package pl.akp.healthybite.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Application-scoped DataStore singleton for persisting session preferences.
 *
 * This Context extension property uses Jetpack's preferencesDataStore delegate,
 * which lazily creates one DataStore instance per process and ties its lifecycle
 * to the Application context. The backing file is named "session.preferences_pb".
 */
val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * Wrapper around Jetpack DataStore that manages the current user session.
 *
 * Exposes reactive [Flow]s that ViewModels use (via `flatMapLatest`) to scope
 * data queries to the logged-in user. On login the user ID is stored; on logout
 * the session is cleared, which cascades through every ViewModel observing [currentUserId].
 */
class SessionDataStore(
    private val dataStore: DataStore<Preferences>
) {

    /**
     * Emits `true` while a user is authenticated.
     *
     * This is a reactive Flow that re-emits automatically whenever the underlying
     * DataStore file changes. SplashViewModel observes this to decide whether to
     * navigate to the Home screen or the Login screen on app launch.
     */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SessionKeys.IS_LOGGED_IN] ?: false
    }

    /**
     * Emits the currently logged-in user's database ID, or `null` if no session exists.
     *
     * This is a reactive Flow observed by every feature ViewModel (via flatMapLatest)
     * to scope data queries to the active user. When the value changes (login/logout),
     * all downstream collectors automatically re-query with the new user ID.
     */
    val currentUserId: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[SessionKeys.CURRENT_USER_ID]
    }

    /**
     * Persists a new session after successful login or registration.
     *
     * Called by AuthRepositoryImpl.login() and AuthRepositoryImpl.register() once
     * credentials are verified / the new user row is inserted. Both keys are written
     * atomically inside a single DataStore edit transaction.
     */
    suspend fun setLoggedIn(userId: Long) {
        dataStore.edit { prefs ->
            prefs[SessionKeys.IS_LOGGED_IN] = true          // mark session as active
            prefs[SessionKeys.CURRENT_USER_ID] = userId     // store the user's DB primary key
        }
    }

    /**
     * Ends the current session (logout). Keeps the DataStore file but resets flags.
     *
     * Called by AuthRepositoryImpl.logout(). Sets the logged-in flag to false and
     * removes the user ID key entirely. This causes currentUserId to emit null,
     * which cascades through every ViewModel that observes it.
     */
    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs[SessionKeys.IS_LOGGED_IN] = false         // flag session as inactive
            prefs.remove(SessionKeys.CURRENT_USER_ID)       // remove user ID so currentUserId emits null
        }
    }

    /**
     * Wipes all stored preferences (used when the persisted user no longer exists in DB).
     *
     * Unlike clearSession(), this removes every key from the file. It is a safety
     * net for the edge case where the DataStore references a userId that was deleted
     * from the Room database (e.g. after a destructive migration during development).
     */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
