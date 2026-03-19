package pl.akp.healthybite.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Splash / startup screen shown while the app seeds the database and
 * checks the user session. Automatically navigates to Login or Home
 * once [SplashViewModel] resolves the destination.
 */
// First screen the user sees on every app launch.
// Displays the app title and either a loading spinner or an error message with a Retry button.
// Once the ViewModel resolves where to go, this composable triggers navigation automatically.
@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // Converts the ViewModel's StateFlow into Compose State so that any change
    // to uiState triggers a recomposition of this composable.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Watches the destination field; every time it changes, the when block runs.
    // This is how navigation is triggered once the ViewModel finishes startup.
    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            // Session was not found — navigate to the login screen.
            SplashDestination.Login -> onNavigateToLogin()
            // Valid session exists — navigate directly to the home screen.
            SplashDestination.Home -> onNavigateToHome()
            // Still initialising — do nothing and keep showing the splash UI.
            SplashDestination.Loading -> { /* waiting */ }
        }
    }

    // Centered layout that fills the entire screen.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // App title displayed prominently in the centre of the splash screen.
            Text(
                text = "HealthyBite",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Error branch: shows the error message and a Retry button so the user
            // can re-trigger the startup sequence without restarting the app.
            if (uiState.error != null) {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = viewModel::onRetry) {
                    Text("Retry")
                }
            } else if (uiState.isLoading) {
                // Loading branch: shows a spinning indicator while the ViewModel
                // seeds the database and checks the session.
                CircularProgressIndicator()
            }
        }
    }
}
