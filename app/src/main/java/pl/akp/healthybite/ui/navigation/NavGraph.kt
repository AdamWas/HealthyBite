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
import pl.akp.healthybite.ui.meals.AddMealViewModel
import pl.akp.healthybite.ui.profile.ProfileScreen
import pl.akp.healthybite.ui.profile.ProfileViewModel
import pl.akp.healthybite.ui.splash.SplashScreen
import pl.akp.healthybite.ui.splash.SplashViewModel

/**
 * Root-level navigation graph.
 *
 * Flow: Splash → (session check) → Login ↔ Register → Main.
 * Profile and AddMeal are top-level destinations pushed onto the Main back-stack.
 *
 * ViewModel factories are created here using dependencies from [HealthyBiteApplication].
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Route.Splash.route
) {
    /*
     * Obtain the custom Application subclass so we can access the
     * hand-wired dependency graph (sessionStore, database DAOs,
     * authRepository, databaseSeeder) without a DI framework.
     */
    val app = LocalContext.current.applicationContext as HealthyBiteApplication

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ── Splash ─────────────────────────────────────────────────────
        // First destination the user sees. SplashViewModel reads the
        // persisted session from DataStore:
        //   • session found   → navigate straight to Main (home tabs)
        //   • no session      → navigate to Login
        composable(Route.Splash.route) {
            val vm: SplashViewModel = viewModel(
                factory = SplashViewModel.Factory(
                    app.sessionStore,
                    app.database.userDao(),
                    app.databaseSeeder
                )
            )
            SplashScreen(
                viewModel = vm,
                onNavigateToLogin = {
                    navController.navigate(Route.Login.route) {
                        // Remove Splash from the back stack so pressing
                        // Back on Login exits the app instead of returning
                        // to the splash animation.
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Route.Main.route) {
                        // Same idea – Splash is a one-shot screen.
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ──────────────────────────────────────────────────────
        // Authenticates the user via AuthViewModel → AuthRepository.
        // On success → Main.  The user can also tap "Register" to create
        // a new account.
        composable(Route.Login.route) {
            val vm: AuthViewModel = viewModel(
                factory = AuthViewModel.Factory(app.authRepository)
            )
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = {
                    navController.navigate(Route.Main.route) {
                        // Pop Login so the user can't press Back to return
                        // to the login form after authenticating.
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    // Push Register on top of Login so the system Back
                    // button naturally returns to Login.
                    navController.navigate(Route.Register.route)
                }
            )
        }

        // ── Register ───────────────────────────────────────────────────
        // New-account creation. RegisterViewModel validates fields and
        // calls AuthRepository.register().
        composable(Route.Register.route) {
            val vm: RegisterViewModel = viewModel(
                factory = RegisterViewModel.Factory(app.authRepository)
            )
            RegisterScreen(
                viewModel = vm,
                onRegisterSuccess = {
                    navController.navigate(Route.Main.route) {
                        // Pop all the way back to (and including) Login so
                        // the back stack is clean: Main is now the root.
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Simply pop Register to reveal the Login screen that
                    // is already on the back stack underneath.
                    navController.popBackStack()
                }
            )
        }

        // ── Main (tab container) ───────────────────────────────────────
        // Hosts MainScaffold which provides a Scaffold with a
        // BottomNavigationBar and its *own* inner NavHost for the five
        // tab destinations (Home, Log, Shopping, Water, Plans).
        // Profile and AddMeal are pushed onto the *root* NavHost on top
        // of Main, so the bottom bar is hidden on those screens.
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

        // ── Profile ────────────────────────────────────────────────────
        // Shows user info and a logout button.
        // ProfileViewModel reads the current session and exposes a
        // logout action that clears DataStore.
        composable(Route.Profile.route) {
            val vm: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(
                    app.sessionStore,
                    app.authRepository
                )
            )
            ProfileScreen(
                viewModel = vm,
                onLogoutSuccess = {
                    navController.navigate(Route.Login.route) {
                        // popUpTo(0) with inclusive = true clears the
                        // *entire* back stack (0 is the synthetic root ID
                        // of the NavGraph).  This ensures no authenticated
                        // screen remains reachable after logout.
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Add Meal ───────────────────────────────────────────────────
        // Form for logging a new meal entry.  AddMealViewModel needs the
        // session (to know which user), plus meal-template and meal-entry
        // DAOs to read templates and persist the entry.
        composable(Route.AddMeal.route) {
            val vm: AddMealViewModel = viewModel(
                factory = AddMealViewModel.Factory(
                    app.sessionStore,
                    app.database.mealTemplateDao(),
                    app.database.mealEntryDao()
                )
            )
            AddMealScreen(
                viewModel = vm,
                // Pop back to the previous screen (Main/Log tab) after
                // the user saves or cancels.
                onBack = { navController.popBackStack() }
            )
        }
    }
}
