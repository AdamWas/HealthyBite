package pl.akp.healthybite.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SessionKeys {
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
}
