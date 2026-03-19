package pl.akp.healthybite.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Light colour scheme – only primary and secondary are overridden with the
 * app's green brand colours; every other slot (background, surface, error, …)
 * falls back to the Material 3 light defaults.
 */
private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryGreen
)

/**
 * Dark colour scheme prepared for future use.  Currently never selected
 * because [HealthyBiteTheme] defaults [darkTheme] to false.
 * When dark-mode support is added, flip the default or read the system
 * setting via isSystemInDarkTheme().
 */
private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryGreen
)

/**
 * App-wide Material 3 theme wrapper.
 *
 * Applied at the very root of the Compose tree inside [HealthyBiteApp] so
 * that every screen and component inherits the correct colours, typography,
 * and shape tokens.
 *
 * @param darkTheme Defaults to `false` — the app ships in light mode only
 *                  for now.  Pass `true` (or read the system setting) to
 *                  enable the dark colour scheme.
 */
@Composable
fun HealthyBiteTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Pick the colour scheme based on the dark-mode flag.
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}

