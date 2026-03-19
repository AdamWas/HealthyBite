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
import pl.akp.healthybite.domain.repository.EmailAlreadyExistsException

/**
 * ViewModel for the Register screen.
 *
 * Handles form input, delegates to [AuthRepository.register], and maps
 * [EmailAlreadyExistsException] to a user-visible error on the email field.
 */
// Manages the registration form state (email, password, confirm password)
// and delegates account creation to AuthRepository.register().
// Maps EmailAlreadyExistsException to a user-visible error on the email field.
// On success, sets registerSuccess so RegisterScreen navigates to login.
class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Private mutable state — only this ViewModel can emit new values.
    private val _uiState = MutableStateFlow(RegisterUiState())
    // Public read-only projection collected by RegisterScreen to drive the UI.
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Called every time the user types in the email field.
    // Updates the email and clears any previous emailError so the error
    // message disappears while the user is correcting the input.
    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    // Called every time the user types in the password field.
    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    // Called every time the user types in the "confirm password" field.
    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    // Toggles plain-text vs masked display for the password field.
    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    // Toggles plain-text vs masked display for the confirm-password field.
    fun onToggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    // Called when the user taps "Create account".
    // Step 1: Set loading to true and clear any previous email error.
    // Step 2: Call authRepository.register() with the current email and password.
    // Step 3 (success): Set registerSuccess = true so RegisterScreen navigates to login.
    // Step 3 (failure): If the error is EmailAlreadyExistsException, show its message
    //   on the email field; otherwise show a generic "Registration failed" message.
    fun onRegister() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailError = null) }
            authRepository.register(_uiState.value.email, _uiState.value.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                }
                .onFailure { e ->
                    val emailError = when (e) {
                        is EmailAlreadyExistsException -> e.message
                        else -> FirestoreDebug.userMessage(e)
                    }
                    _uiState.update { it.copy(isLoading = false, emailError = emailError) }
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
            return RegisterViewModel(authRepository) as T
        }
    }
}
