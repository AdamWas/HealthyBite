package pl.akp.healthybite.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.akp.healthybite.HealthyBiteApplication
import pl.akp.healthybite.ui.auth.AuthViewModel
import pl.akp.healthybite.ui.auth.LoginScreen
import pl.akp.healthybite.ui.auth.RegisterScreen
import pl.akp.healthybite.ui.home.HomeScreen
import pl.akp.healthybite.ui.log.LogScreen
import pl.akp.healthybite.ui.meals.AddMealScreen
import pl.akp.healthybite.ui.plans.PlansScreen
import pl.akp.healthybite.ui.profile.ProfileScreen
import pl.akp.healthybite.ui.shopping.ShoppingScreen
import pl.akp.healthybite.ui.splash.SplashScreen
import pl.akp.healthybite.ui.splash.SplashViewModel
import pl.akp.healthybite.ui.water.WaterScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Route.Splash.route
) {
    val app = LocalContext.current.applicationContext as HealthyBiteApplication

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.Splash.route) {
            val vm: SplashViewModel = viewModel(
                factory = SplashViewModel.Factory(app.sessionStore, app.database.userDao())
            )
            SplashScreen(
                viewModel = vm,
                onNavigateToLogin = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Login.route) {
            val vm: AuthViewModel = viewModel(
                factory = AuthViewModel.Factory(app.authRepository)
            )
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Register.route) { RegisterScreen() }
        composable(Route.Home.route) { HomeScreen() }
        composable(Route.Log.route) { LogScreen() }
        composable(Route.AddMeal.route) { AddMealScreen() }
        composable(Route.Shopping.route) { ShoppingScreen() }
        composable(Route.Water.route) { WaterScreen() }
        composable(Route.Plans.route) { PlansScreen() }
        composable(Route.Profile.route) { ProfileScreen() }
    }
}
