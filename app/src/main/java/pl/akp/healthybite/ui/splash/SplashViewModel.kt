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

class SplashViewModel(
    private val sessionDataStore: SessionDataStore,
    private val userDao: UserDao,
    private val databaseSeeder: DatabaseSeeder
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        startUp()
    }

    fun onRetry() {
        startUp()
    }

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
