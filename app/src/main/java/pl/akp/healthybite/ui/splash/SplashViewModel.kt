package pl.akp.healthybite.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.db.DatabaseSeeder
import pl.akp.healthybite.data.db.dao.UserDao

/**
 * Performs app startup tasks and decides the initial navigation destination.
 *
 * 1. Seeds the database with demo data if needed ([DatabaseSeeder]).
 * 2. Checks whether a valid session exists in [SessionDataStore].
 * 3. Emits [SplashDestination.Home] or [SplashDestination.Login] accordingly.
 *
 * If the persisted user ID no longer exists in the DB (e.g. after a destructive
 * migration), the session is cleared and the user is sent to Login.
 */
// Runs on every app launch to decide the initial navigation destination.
// Seeds the database with demo data if needed, then checks whether a valid
// user session exists in DataStore, and emits Home or Login accordingly.
class SplashViewModel(
    private val sessionDataStore: SessionDataStore,
    private val userDao: UserDao,
    private val databaseSeeder: DatabaseSeeder
) : ViewModel() {

    // Private mutable state that only this ViewModel can modify.
    private val _uiState = MutableStateFlow(SplashUiState())
    // Public read-only projection exposed to the UI layer (SplashScreen).
    // Compose collects this flow and recomposes whenever the value changes.
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    // The init block runs as soon as the ViewModel is instantiated,
    // so the startup sequence begins automatically without an explicit call.
    init {
        startUp()
    }

    // Called when the user taps the "Retry" button after a startup error.
    // Re-triggers the entire initialisation sequence from scratch.
    fun onRetry() {
        startUp()
    }

    // Orchestrates the full startup sequence inside a coroutine.
    // Step 1: Reset state to loading (clears any previous error).
    // Step 2: Seed the database with initial data if this is a fresh install.
    // Step 3: Check the persisted session to decide Login vs Home.
    // If any step throws, the exception message is surfaced in the UI.
    private fun startUp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, destination = SplashDestination.Loading) }
            try {
                databaseSeeder.seedIfNeeded()
                checkSession()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Initialization failed")
                }
            }
        }
    }

    // Reads the "isLoggedIn" flag from DataStore to determine whether a session exists.
    // If logged in, it also verifies that the user ID still exists in the local DB
    // (it might have been wiped by a destructive Room migration).
    // If the user record is missing, the stale session is cleared and the user
    // is redirected to the login screen instead.
    private suspend fun checkSession() {
        val isLoggedIn = sessionDataStore.isLoggedIn.first()
        if (isLoggedIn) {
            val userId = sessionDataStore.currentUserId.first()
            val userExists = userId != null && userDao.getById(userId) != null
            if (userExists) {
                _uiState.update { it.copy(isLoading = false, destination = SplashDestination.Home) }
            } else {
                sessionDataStore.clear()
                _uiState.update { it.copy(isLoading = false, destination = SplashDestination.Login) }
            }
        } else {
            _uiState.update { it.copy(isLoading = false, destination = SplashDestination.Login) }
        }
    }

    // ViewModelProvider.Factory is required for manual dependency injection (no Hilt).
    // It lets the NavGraph pass SessionDataStore, UserDao, and DatabaseSeeder into
    // the ViewModel constructor, since ViewModelProvider cannot do this by default.
    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val userDao: UserDao,
        private val databaseSeeder: DatabaseSeeder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SplashViewModel(sessionDataStore, userDao, databaseSeeder) as T
        }
    }
}
