package pl.akp.healthybite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pl.akp.healthybite.app.HealthyBiteApp

/**
 * The **only** Activity in the entire app (single-activity architecture).
 *
 * In a single-activity Compose app there are no Fragments or XML layouts. This Activity
 * serves only as the Android entry point; all UI is built declaratively inside Compose
 * composable functions.
 *
 * Lifecycle:
 *   Android creates this Activity → onCreate() is called → setContent installs the
 *   Compose UI tree → HealthyBiteApp() renders everything from the theme down to
 *   individual screens.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent replaces the traditional setContentView(R.layout.activity_main) used
        // with XML layouts. It creates a ComposeView under the hood and starts the Compose
        // runtime, which will recompose the UI whenever state changes.
        setContent {
            // HealthyBiteApp() is the root composable function that renders the ENTIRE UI
            // tree: theme → surface → navigation graph → individual screens.
            HealthyBiteApp()
        }
    }
}

