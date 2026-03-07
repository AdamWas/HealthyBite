package pl.akp.healthybite.ui.auth

/** UI state for the login screen – tracks form fields, loading, errors, and success flag. */
data class AuthUiState(
    // Current text in the email input field, bound two-way via onEmailChanged().
    val email: String = "",
    // Current text in the password input field, bound two-way via onPasswordChanged().
    val password: String = "",
    // Controls whether the password field shows plain text or dots.
    // Toggled by the visibility icon button next to the password field.
    val isPasswordVisible: Boolean = false,
    // True while the login request is in flight; disables the Sign In button
    // and shows a spinner instead of the button label.
    val isLoading: Boolean = false,
    // Non-null when the repository returns a login failure (e.g. "Invalid email or password").
    // Displayed below the password field and cleared when the user edits either field.
    val credentialError: String? = null,
    /** Set to `true` once login succeeds; observed by [LoginScreen] to trigger navigation. */
    // LoginScreen watches this flag in a LaunchedEffect to navigate to the home screen.
    val loginSuccess: Boolean = false
)
