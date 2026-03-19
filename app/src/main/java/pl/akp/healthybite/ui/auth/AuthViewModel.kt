package pl.akp.healthybite.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.akp.healthybite.data.firebase.FirestoreDebug
import pl.akp.healthybite.domain.repository.AuthRepository

/**
 * ViewModel for the Login screen.
 *
 * Manages email/password form state and delegates authentication to [AuthRepository].
 * On success, sets [AuthUiState.loginSuccess] which the screen observes to navigate away.
 */
// Manages the login form state (email, password, visibility, errors) and
// delegates credential verification to AuthRepository.
// On successful login the loginSuccess flag is set, which the LoginScreen
// observes in a LaunchedEffect to navigate to the home screen.
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Private mutable state — only this ViewModel can emit new values.
    private val _uiState = MutableStateFlow(AuthUiState())
    // Public read-only projection collected by LoginScreen to drive the UI.
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Called every time the user types in the email field.
    // Updates the email value and clears any previous credential error so the
    // error message disappears as soon as the user starts editing.
    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, credentialError = null) }
    }

    // Called every time the user types in the password field.
    // Updates the password value and clears any previous credential error.
    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, credentialError = null) }
    }

    // Called when the user taps the eye icon next to the password field.
    // Flips isPasswordVisible so the field switches between plain text and masked dots.
    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    // Called when the user taps "Sign In" or presses Done on the keyboard.
    // Step 1: Set loading to true and clear any previous error.
    // Step 2: Call authRepository.login() with the current email and password.
    // Step 3 (success): Set loginSuccess = true so LoginScreen navigates away.
    // Step 3 (failure): Store the error message in credentialError for display.
    fun onLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, credentialError = null) }
            authRepository.login(_uiState.value.email, _uiState.value.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            credentialError = FirestoreDebug.userMessage(e)
                        )
                    }
                }
        }
    }

    // ViewModelProvider.Factory for manual dependency injection (no Hilt).
    // Allows the navigation graph to pass AuthRepository into the ViewModel
    // constructor, which ViewModelProvider cannot do by default.
    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
