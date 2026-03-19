package pl.akp.healthybite.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import pl.akp.healthybite.ui.navigation.NavGraph
import pl.akp.healthybite.ui.theme.HealthyBiteTheme

/**
 * Root composable that applies the app-wide Material 3 theme and hosts the
 * top-level [NavGraph] which manages all screen navigation.
 *
 * Called directly from MainActivity.setContent{} – this is the single entry point for the
 * entire Compose UI tree.
 */
@Composable
fun HealthyBiteApp() {
    // HealthyBiteTheme wraps every descendant composable in the app's Material 3 theme.
    // It supplies the custom green colour palette, typography, and shape definitions so
    // that all Material components (buttons, cards, navigation bars, etc.) pick up the
    // brand colours automatically via MaterialTheme.colorScheme.
    HealthyBiteTheme {
        // Surface provides a background colour (colorScheme.background) and applies
        // content-colour defaults for child text/icons. fillMaxSize() makes it span the
        // full screen so no un-themed pixels are visible.
        Surface(modifier = Modifier.fillMaxSize()) {
            // rememberNavController() creates a NavHostController that survives
            // recompositions. This is the ROOT (top-level) navigation controller used by
            // NavGraph to navigate between auth screens, the main scaffold, and overlays
            // like the Add Meal screen.
            val navController = rememberNavController()

            // NavGraph defines every screen destination and the transitions between them.
            // It receives the root navController so any screen can trigger navigation
            // (e.g. login → home, home → add-meal).
            NavGraph(navController = navController)
        }
    }
}
