package pl.akp.healthybite.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Profile screen displaying account info, a daily calorie goal editor, and a logout button.
 *
 * Snackbar feedback is shown after saving the goal or on errors.
 * Logout clears the DataStore session and navigates back to Login.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Manages the Snackbar queue; remembered so it survives recomposition.
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate to Login once the logout flow completes successfully.
    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) onLogoutSuccess()
    }

    // Edge-case guard: if there's no session in the DataStore (e.g. after a destructive DB
    // migration), automatically redirect to Login instead of showing an empty profile.
    LaunchedEffect(uiState.noSession) {
        if (uiState.noSession) onLogoutSuccess()
    }

    // Show a Snackbar whenever a success or error message is emitted by the ViewModel,
    // then clear the message so it isn't re-shown on the next recomposition.
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // Full-screen spinner shown while the user profile is being loaded.
        // The email.isEmpty() guard prevents flashing the spinner during a goal-save reload.
        if (uiState.isLoading && uiState.email.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AccountCard(email = uiState.email)

                GoalsCard(
                    caloriesGoal = uiState.caloriesGoal,
                    caloriesGoalError = uiState.caloriesGoalError,
                    saving = uiState.saving,
                    onGoalChanged = viewModel::onGoalChanged,
                    onSaveClicked = viewModel::onSaveClicked
                )

                SessionCard(
                    isLoading = uiState.isLoading,
                    onLogout = viewModel::onLogout
                )
            }
        }
    }
}

/**
 * Displays the user's email address with an email icon.
 * Read-only — there is no way to change the email from this screen.
 */
@Composable
private fun AccountCard(email: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Editable card for the daily calorie goal.
 *
 * Contains a numeric OutlinedTextField (valid range 1000–5000 kcal) and a Save button.
 * While saving, the button shows a spinner and is disabled. Validation errors appear
 * as supporting text below the field.
 */
@Composable
private fun GoalsCard(
    caloriesGoal: String,
    caloriesGoalError: String?,
    saving: Boolean,
    onGoalChanged: (String) -> Unit,
    onSaveClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Goals",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = caloriesGoal,
                onValueChange = onGoalChanged,
                label = { Text("Daily calorie goal") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null
                    )
                },
                suffix = { Text("kcal") },
                isError = caloriesGoalError != null,
                supportingText = caloriesGoalError?.let { error ->
                    { Text(error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSaveClicked,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}

/**
 * Card containing the logout button.
 *
 * The button is styled with the error color scheme (red) to signal a destructive action.
 * While the logout coroutine is running, a spinner replaces the button label to prevent
 * duplicate taps.
 */
@Composable
private fun SessionCard(
    isLoading: Boolean,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Session",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLogout,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Log out")
                }
            }
        }
    }
}
