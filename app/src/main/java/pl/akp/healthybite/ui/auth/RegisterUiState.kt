package pl.akp.healthybite.ui.auth

/**
 * UI state for the registration screen.
 *
 * Adds a [confirmPassword] field and [emailError] (for duplicate email feedback)
 * on top of the standard form fields.
 */
data class RegisterUiState(
    // Current text in the email input field, bound two-way via onEmailChanged().
    val email: String = "",
    // Current text in the password input field, bound two-way via onPasswordChanged().
    val password: String = "",
    // Current text in the "confirm password" input field.
    // RegisterScreen compares this with password to show a mismatch error.
    val confirmPassword: String = "",
    // Controls plain-text vs masked display for the password field.
    val isPasswordVisible: Boolean = false,
    // Controls plain-text vs masked display for the confirm-password field.
    val isConfirmPasswordVisible: Boolean = false,
    // Non-null when a server-side error occurs on the email field
    // (e.g. "Email already registered"). Also used for format errors in the UI.
    val emailError: String? = null,
    // True while the registration request is in flight; disables form inputs
    // and shows a spinner inside the "Create account" button.
    val isLoading: Boolean = false,
    // Set to true once registration succeeds; RegisterScreen watches this flag
    // in a LaunchedEffect to navigate to the login screen.
    val registerSuccess: Boolean = false
)
