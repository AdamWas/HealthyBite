package pl.akp.healthybite.ui.splash

sealed interface SplashDestination {
    data object Loading : SplashDestination
    data object Login : SplashDestination
    data object Home : SplashDestination
}

data class SplashUiState(
    val destination: SplashDestination = SplashDestination.Loading
)
