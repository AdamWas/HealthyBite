package pl.akp.healthybite.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.firebase.FirestoreDebug
import pl.akp.healthybite.data.repository.FirestoreSeeder
import pl.akp.healthybite.domain.repository.AuthRepository

class SplashViewModel(
    private val sessionDataStore: SessionDataStore,
    private val authRepository: AuthRepository,
    private val firestoreSeeder: FirestoreSeeder
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
                firestoreSeeder.seedIfNeeded()
                checkSession()
            } catch (e: Exception) {
                Log.e(SPLASH_TAG, "startUp failed", e)
                FirestoreDebug.log("SplashViewModel.startUp", e)
                _uiState.update {
                    it.copy(isLoading = false, error = FirestoreDebug.userMessage(e))
                }
            }
        }
    }

    private suspend fun checkSession() {
        try {
            val isLoggedIn = sessionDataStore.isLoggedIn.first()
            Log.d(SPLASH_TAG, "checkSession isLoggedIn=$isLoggedIn")
            if (isLoggedIn) {
                val userId = sessionDataStore.currentUserId.first()
                Log.d(SPLASH_TAG, "checkSession userId present=${userId != null}")
                val userExists = userId != null && authRepository.getUser(userId) != null
                if (userExists) {
                    _uiState.update { it.copy(isLoading = false, destination = SplashDestination.Home) }
                } else {
                    sessionDataStore.clear()
                    _uiState.update { it.copy(isLoading = false, destination = SplashDestination.Login) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, destination = SplashDestination.Login) }
            }
        } catch (e: Exception) {
            Log.e(SPLASH_TAG, "checkSession failed", e)
            FirestoreDebug.log("SplashViewModel.checkSession", e)
            try {
                sessionDataStore.clear()
            } catch (clearErr: Exception) {
                Log.e(SPLASH_TAG, "session clear after checkSession failure", clearErr)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    destination = SplashDestination.Login,
                    error = FirestoreDebug.userMessage(e)
                )
            }
        }
    }

    private companion object {
        private const val SPLASH_TAG = "HealthyBiteSplash"
    }

    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val authRepository: AuthRepository,
        private val firestoreSeeder: FirestoreSeeder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SplashViewModel(sessionDataStore, authRepository, firestoreSeeder) as T
        }
    }
}
