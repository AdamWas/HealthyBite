package pl.akp.healthybite.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.akp.healthybite.domain.validation.EmailValidator
import pl.akp.healthybite.domain.validation.PasswordValidator

/**
 * Registration screen with email, password, and confirm-password fields.
 *
 * Password complexity rules from [PasswordValidator] are shown inline as
 * the user types. The "Create account" button is only enabled once all
 * validation passes (valid email, strong password, matching confirmation).
 */
// Registration screen composable with email, password, and confirm-password fields.
// Password complexity rules from PasswordValidator are shown inline as the user types.
// The "Create account" button is only enabled once all validations pass.
// On success, navigates to the login screen so the user can sign in with the new account.
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Converts the ViewModel's StateFlow into Compose State so that any change
    // to uiState triggers a recomposition of this composable.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Watches the registerSuccess flag. When it flips to true (after a successful
    // registration), this effect fires and calls onRegisterSuccess to navigate away.
    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) onRegisterSuccess()
    }

    // Client-side email format check — true when the email is non-blank but fails
    // the EmailValidator regex (e.g. missing "@" or domain part).
    val showEmailFormatError =
        uiState.email.isNotBlank() && !EmailValidator.isValid(uiState.email)
    // Combines server-side emailError (e.g. "Email already registered") with the
    // client-side format error. Server-side errors take priority when both exist.
    val emailError =
        uiState.emailError ?: if (showEmailFormatError) "Enter a valid email address" else null

    // Real-time password strength checking using PasswordValidator rules
    // (minimum length, uppercase, lowercase, digit, special character).
    // Returns a list of human-readable messages for rules that are NOT yet satisfied.
    val passwordMissing = if (uiState.password.isNotBlank()) {
        PasswordValidator.missingRules(uiState.password).map { it.message }
    } else {
        emptyList()
    }

    // Password confirmation validation — shows "Passwords do not match" once the
    // user has started typing in the confirm field and the values diverge.
    val confirmError =
        if (uiState.confirmPassword.isNotBlank() && uiState.confirmPassword != uiState.password) {
            "Passwords do not match"
        } else {
            null
        }

    // The "Create account" button is enabled only when ALL of these conditions pass:
    // 1. Email is not blank and passes format validation.
    // 2. Password is not blank and satisfies all PasswordValidator rules.
    // 3. Confirm password matches the password.
    // 4. A registration request is not already in progress.
    val isRegisterEnabled = uiState.email.isNotBlank()
            && EmailValidator.isValid(uiState.email)
            && uiState.password.isNotBlank()
            && PasswordValidator.isValid(uiState.password)
            && uiState.confirmPassword == uiState.password
            && !uiState.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HealthyBite",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your account",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email input field with both client-side format validation and server-side
        // duplicate-email error display. Disabled while a request is in flight.
        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Email") },
            singleLine = true,
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = emailError != null,
            supportingText = if (emailError != null) {
                { Text(emailError) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password input field with inline display of unsatisfied password rules.
        // Each missing rule (e.g. "Must contain a digit") is listed below the field.
        // Includes a visibility toggle icon to switch between masked and plain text.
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Password") },
            singleLine = true,
            enabled = !uiState.isLoading,
            visualTransformation = if (uiState.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (uiState.isPasswordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            isError = passwordMissing.isNotEmpty(),
            supportingText = if (passwordMissing.isNotEmpty()) {
                {
                    Column {
                        passwordMissing.forEach { msg ->
                            Text(text = msg)
                        }
                    }
                }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm-password field — must match the password above.
        // Shows "Passwords do not match" when the values diverge.
        // The IME action is Done — pressing it triggers registration if the button is enabled.
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChanged,
            label = { Text("Confirm password") },
            singleLine = true,
            enabled = !uiState.isLoading,
            visualTransformation = if (uiState.isConfirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = viewModel::onToggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.isConfirmPasswordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (uiState.isConfirmPasswordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (isRegisterEnabled) viewModel.onRegister() }
            ),
            isError = confirmError != null,
            supportingText = if (confirmError != null) {
                { Text(confirmError) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // "Create account" button — disabled until isRegisterEnabled is true.
        // While a registration request is in flight, the label is replaced with
        // a spinning progress indicator to give the user visual feedback.
        Button(
            onClick = viewModel::onRegister,
            enabled = isRegisterEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Create account")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation link back to the login screen for users who already have
        // an account. Calls onNavigateToLogin to pop back to the Login route.
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log in")
        }
    }
}
