package pl.akp.healthybite.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.akp.healthybite.domain.repository.AuthRepository
import pl.akp.healthybite.domain.repository.EmailAlreadyExistsException

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

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
                        else -> "Registration failed"
                    }
                    _uiState.update { it.copy(isLoading = false, emailError = emailError) }
                }
        }
    }

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RegisterViewModel(authRepository) as T
        }
    }
}
