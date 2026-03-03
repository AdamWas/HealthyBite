package pl.akp.healthybite.ui.auth

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val credentialError: String? = null,
    val loginSuccess: Boolean = false
)
