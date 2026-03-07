package pl.akp.healthybite.ui.splash

/** Possible navigation targets resolved by [SplashViewModel] after initialisation. */
// Sealed interface restricts all possible outcomes to three known states,
// ensuring the when-block in SplashScreen is exhaustive at compile time.
sealed interface SplashDestination {
    // Still initialising — the splash screen shows a loading spinner.
    data object Loading : SplashDestination
    // No active session was found — the user should be sent to the login screen.
    data object Login : SplashDestination
    // A valid session exists — the user can skip login and go straight to the home screen.
    data object Home : SplashDestination
}

/** UI state for the splash screen – drives the loading indicator, error banner, and navigation. */
data class SplashUiState(
    // Determines where the splash screen should navigate once initialisation finishes.
    // Starts as Loading and is updated to Login or Home by SplashViewModel.
    val destination: SplashDestination = SplashDestination.Loading,
    // True while the database seeding and session check are in progress; shows a spinner.
    val isLoading: Boolean = true,
    // Non-null when an error occurred during startup; displays the message with a Retry button.
    val error: String? = null
)
