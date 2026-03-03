package pl.akp.healthybite.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.akp.healthybite.HealthyBiteApplication
import pl.akp.healthybite.app.MainScaffold
import pl.akp.healthybite.ui.auth.AuthViewModel
import pl.akp.healthybite.ui.auth.LoginScreen
import pl.akp.healthybite.ui.auth.RegisterScreen
import pl.akp.healthybite.ui.auth.RegisterViewModel
import pl.akp.healthybite.ui.meals.AddMealScreen
import pl.akp.healthybite.ui.profile.ProfileScreen
import pl.akp.healthybite.ui.profile.ProfileViewModel
import pl.akp.healthybite.ui.splash.SplashScreen
import pl.akp.healthybite.ui.splash.SplashViewModel

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
                    navController.navigate(Route.Main.route) {
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
                    navController.navigate(Route.Main.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Register.route)
                }
            )
        }

        composable(Route.Register.route) {
            val vm: RegisterViewModel = viewModel(
                factory = RegisterViewModel.Factory(app.authRepository)
            )
            RegisterScreen(
                viewModel = vm,
                onRegisterSuccess = {
                    navController.navigate(Route.Main.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.Main.route) {
            MainScaffold(
                onNavigateToProfile = {
                    navController.navigate(Route.Profile.route)
                },
                onNavigateToAddMeal = {
                    navController.navigate(Route.AddMeal.route)
                }
            )
        }

        composable(Route.Profile.route) {
            val vm: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(app.authRepository)
            )
            ProfileScreen(
                viewModel = vm,
                onLogoutSuccess = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.AddMeal.route) { AddMealScreen() }
    }
}
