package pl.akp.healthybite.ui.profile

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
import pl.akp.healthybite.domain.repository.AuthRepository

class ProfileViewModel(
    private val sessionDataStore: SessionDataStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val userId = sessionDataStore.currentUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, noSession = true) }
                return@launch
            }
            currentUserId = userId

            try {
                val user = authRepository.getUser(userId)
                if (user == null) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "User not found")
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        email = user.email,
                        caloriesGoal = user.dailyCaloriesGoal.toString()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load profile")
                }
            }
        }
    }

    fun onGoalChanged(text: String) {
        _uiState.update {
            it.copy(
                caloriesGoal = text,
                caloriesGoalError = null,
                successMessage = null
            )
        }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        val goal = state.caloriesGoal.trim().toIntOrNull()

        if (goal == null) {
            _uiState.update { it.copy(caloriesGoalError = "Must be a number") }
            return
        }
        if (goal < 1000 || goal > 5000) {
            _uiState.update { it.copy(caloriesGoalError = "Must be between 1000 and 5000") }
            return
        }

        val userId = currentUserId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, caloriesGoalError = null) }
            try {
                authRepository.updateCaloriesGoal(userId, goal)
                _uiState.update {
                    it.copy(saving = false, successMessage = "Goal updated")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(saving = false, errorMessage = "Failed to save goal")
                }
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoading = false, logoutSuccess = true) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(sessionDataStore, authRepository) as T
        }
    }
}
