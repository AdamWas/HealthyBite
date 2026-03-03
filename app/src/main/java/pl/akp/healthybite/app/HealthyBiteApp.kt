package pl.akp.healthybite.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import pl.akp.healthybite.ui.navigation.NavGraph
import pl.akp.healthybite.ui.theme.HealthyBiteTheme

@Composable
fun HealthyBiteApp() {
    HealthyBiteTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            NavGraph(navController = navController)
        }
    }
}
