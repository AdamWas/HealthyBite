package pl.akp.healthybite.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

/** Preference key constants used by [SessionDataStore]. */
object SessionKeys {
    /** Boolean flag: true when a user is authenticated, false after logout. */
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    /** The Room primary key (Long) of the currently logged-in user, used to scope all data queries. */
    val CURRENT_USER_ID = longPreferencesKey("current_user_id")
}
