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
import pl.akp.healthybite.data.db.dao.UserDao

class SplashViewModel(
    private val sessionDataStore: SessionDataStore,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val isLoggedIn = sessionDataStore.isLoggedIn.first()
            if (isLoggedIn) {
                val userId = sessionDataStore.currentUserId.first()
                val userExists = userId != null && userDao.getById(userId) != null
                if (userExists) {
                    _uiState.update { it.copy(destination = SplashDestination.Home) }
                } else {
                    sessionDataStore.clear()
                    _uiState.update { it.copy(destination = SplashDestination.Login) }
                }
            } else {
                _uiState.update { it.copy(destination = SplashDestination.Login) }
            }
        }
    }

    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val userDao: UserDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SplashViewModel(sessionDataStore, userDao) as T
        }
    }
}
