package pl.akp.healthybite.ui.auth

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false
)
