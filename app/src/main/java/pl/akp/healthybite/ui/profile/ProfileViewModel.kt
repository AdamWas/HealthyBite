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

/**
 * ViewModel for the Profile screen.
 *
 * Loads the current user's email and daily calorie goal from [AuthRepository],
 * validates and persists goal changes, and handles logout via session clearing.
 */
class ProfileViewModel(
    private val sessionDataStore: SessionDataStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Cached locally so onSaveClicked() doesn't need to re-read the DataStore on every tap.
    private var currentUserId: Long? = null

    init {
        // Load the user profile as soon as the ViewModel is created (screen appears).
        loadUser()
    }

    /**
     * Reads the userId from the DataStore session, then fetches the full User from the repository.
     *
     * Edge cases handled:
     * - No session (userId == null) → sets [ProfileUiState.noSession] so the UI redirects to Login.
     * - User not found in the DB  → shows an error message (could happen after a DB wipe).
     * - Repository exception      → generic "Failed to load profile" error.
     */
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

    /**
     * Called on every keystroke in the calorie-goal text field.
     * Updates the field value and clears any previous validation error or success message
     * so the user gets immediate feedback that their input is being accepted.
     */
    fun onGoalChanged(text: String) {
        _uiState.update {
            it.copy(
                caloriesGoal = text,
                caloriesGoalError = null,
                successMessage = null
            )
        }
    }

    /**
     * Validates and persists the daily calorie goal.
     *
     * Validation rules (synchronous, before launching coroutine):
     *   1. Must parse to an integer.
     *   2. Must be in the 1000–5000 kcal range (reasonable daily intake).
     *
     * On success, delegates to [AuthRepository.updateCaloriesGoal] and shows a
     * "Goal updated" snackbar. On failure, shows a generic error snackbar.
     */
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

        val userId = currentUserId ?: return // guard: should never be null at this point

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

    /**
     * Logs the user out by calling [AuthRepository.logout], which clears the DataStore session.
     * Once complete, sets [ProfileUiState.logoutSuccess] so the UI navigates back to Login.
     */
    fun onLogout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoading = false, logoutSuccess = true) }
        }
    }

    /**
     * Resets transient messages after the Snackbar has been displayed.
     * Called from the LaunchedEffect in ProfileScreen to prevent re-showing
     * the same message on recomposition.
     */
    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    /**
     * Manual dependency-injection factory.
     * The NavGraph instantiates this with the app-level SessionDataStore and AuthRepository.
     */
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
