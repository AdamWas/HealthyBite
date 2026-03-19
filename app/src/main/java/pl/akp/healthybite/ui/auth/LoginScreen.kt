package pl.akp.healthybite.ui.auth

import androidx.compose.animation.AnimatedVisibility
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

/**
 * Login screen with email + password fields, inline validation, and
 * a link to the registration screen.
 *
 * Navigation is driven by observing [AuthUiState.loginSuccess] in a [LaunchedEffect].
 */
// Login screen composable with email and password fields, inline validation,
// a credential error banner, and a link to the registration screen.
// Navigation to the home screen is driven by observing loginSuccess in a LaunchedEffect.
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    // Converts the ViewModel's StateFlow into Compose State so that any change
    // to uiState triggers a recomposition of this composable.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Watches the loginSuccess flag. When it flips to true (after a successful
    // login), this effect fires and calls onLoginSuccess to navigate to the home screen.
    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) onLoginSuccess()
    }

    // Real-time email format validation: shows an error as soon as the user
    // types something that does not look like a valid email address.
    // The error only appears once the field is non-blank to avoid flagging an empty field.
    val showEmailError = uiState.email.isNotBlank() && !EmailValidator.isValid(uiState.email)

    // The Sign In button is enabled only when all of these conditions are met:
    // 1. Email is not empty.
    // 2. Password is not empty.
    // 3. Email passes format validation.
    // 4. A login request is not already in progress.
    val isLoginEnabled = uiState.email.isNotBlank()
            && uiState.password.isNotBlank()
            && !showEmailError
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
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email input field with real-time format validation via EmailValidator.
        // Shows "Invalid email format" below the field when the format is wrong.
        // Keyboard type is set to Email for a better soft-keyboard layout,
        // and IME action is Next to move focus to the password field.
        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = showEmailError,
            supportingText = if (showEmailError) {
                { Text("Invalid email format") }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password input field with a toggle-visibility icon button.
        // When visible, displays plain text; otherwise masks characters with dots.
        // The IME action is Done — pressing it triggers login if the button is enabled.
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Password") },
            singleLine = true,
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
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (isLoginEnabled) viewModel.onLogin() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Credential error banner (e.g. "Invalid email or password") returned by
        // the repository after a failed login attempt. Animated in/out for a smooth UX.
        AnimatedVisibility(visible = uiState.credentialError != null) {
            Text(
                text = uiState.credentialError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In button — disabled until isLoginEnabled is true.
        // While a login request is in flight, the label is replaced with a
        // spinning progress indicator to give the user visual feedback.
        Button(
            onClick = viewModel::onLogin,
            enabled = isLoginEnabled,
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
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation link to the registration screen for users who don't have
        // an account yet. Calls onNavigateToRegister to push the Register route.
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}
