package pl.akp.healthybite.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pl.akp.healthybite.HealthyBiteApplication
import pl.akp.healthybite.ui.home.HomeScreen
import pl.akp.healthybite.ui.home.HomeViewModel
import pl.akp.healthybite.ui.log.LogScreen
import pl.akp.healthybite.ui.log.LogViewModel
import pl.akp.healthybite.ui.navigation.Route
import pl.akp.healthybite.ui.plans.PlansScreen
import pl.akp.healthybite.ui.plans.PlansViewModel
import pl.akp.healthybite.ui.shopping.ShoppingScreen
import pl.akp.healthybite.ui.shopping.ShoppingViewModel
import pl.akp.healthybite.ui.water.WaterScreen
import pl.akp.healthybite.ui.water.WaterViewModel

/**
 * Describes a single item in the bottom navigation bar.
 *
 * Each instance maps a Route string to the Material icon and human-readable label that
 * appear in the NavigationBar. The list of these items (below) defines the order and
 * content of the five tabs.
 */
private data class BottomNavItem(
    val route: String,       // navigation route string that matches a composable() destination
    val icon: ImageVector,   // Material icon displayed in the tab
    val label: String        // text shown below the icon
)

/**
 * The five primary tabs shown in the bottom navigation bar after login.
 *
 * Order here determines the visual order of tabs from left to right. Each entry's route
 * must match a composable() destination registered in the inner NavHost below.
 */
private val bottomNavItems = listOf(
    BottomNavItem(Route.Home.route, Icons.Filled.Home, "Home"),
    BottomNavItem(Route.Log.route, Icons.Filled.MenuBook, "Log"),
    BottomNavItem(Route.Shopping.route, Icons.Filled.ShoppingCart, "Shopping"),
    BottomNavItem(Route.Water.route, Icons.Filled.WaterDrop, "Water"),
    BottomNavItem(Route.Plans.route, Icons.Filled.CalendarMonth, "Plans"),
)

/**
 * The main authenticated shell layout the user sees after logging in.
 *
 * This composable is the "chrome" around every tab screen. It provides:
 *   - A top app bar showing the app name and a profile icon button
 *   - A 5-tab bottom navigation bar (Home, Log, Shopping, Water, Plans)
 *   - A floating action button (FAB) for quickly adding a meal
 *   - An **inner** NavHost that swaps tab content without leaving this scaffold
 *
 * Data flow overview:
 *   HealthyBiteApplication (DI container)
 *     → ViewModel.Factory (receives DAOs / SessionStore from the Application)
 *       → ViewModel (holds screen state, exposes Flows / StateFlows)
 *         → Screen composable (observes state, renders UI, sends events back to VM)
 *
 * @param onNavigateToProfile called when the user taps the profile icon – navigates in
 *        the ROOT NavHost (not the inner one), so the profile screen replaces the scaffold.
 * @param onNavigateToAddMeal called when the user taps the FAB – also navigates in the
 *        ROOT NavHost to the Add Meal screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    onNavigateToProfile: () -> Unit,
    onNavigateToAddMeal: () -> Unit
) {
    // Cast applicationContext to HealthyBiteApplication to access the manual DI container.
    // This gives us the Room database, SessionDataStore, and repositories without Hilt/Koin.
    val app = LocalContext.current.applicationContext as HealthyBiteApplication

    // innerNav is a SEPARATE NavController from the root one created in HealthyBiteApp.kt.
    // The root controller handles top-level navigation (splash → login → main scaffold → add meal),
    // while innerNav handles only the tab switching WITHIN this scaffold.
    // This separation means switching tabs does not affect the root back stack.
    val innerNav = rememberNavController()

    // currentBackStackEntryAsState() returns a State<NavBackStackEntry?> that recomposes this
    // composable every time the user switches tabs. We use it to highlight the selected tab.
    val backStackEntry by innerNav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route // e.g. "home", "log", "water"

    Scaffold(
        // ── TOP BAR ──
        // Displays the app name centred and a profile icon button on the right.
        // Tapping the profile icon calls onNavigateToProfile, which navigates to the
        // ProfileScreen in the ROOT NavHost (outside this scaffold).
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "HealthyBite",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    // Profile icon button – triggers root-level navigation to ProfileScreen
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },

        // ── BOTTOM BAR ──
        // Material 3 NavigationBar with 5 tabs. Each tab maps to a BottomNavItem defined above.
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        // Highlight this tab if its route matches the currently displayed destination
                        selected = currentRoute == item.route,
                        onClick = {
                            // Navigate to the tapped tab's route within the INNER NavHost.
                            innerNav.navigate(item.route) {
                                // popUpTo the start destination (Home) to avoid building up a
                                // huge back stack as the user taps different tabs repeatedly.
                                // saveState = true persists the tab's scroll position, ViewModel
                                // state, etc. so it is restored when the user comes back.
                                popUpTo(innerNav.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // launchSingleTop prevents creating a duplicate instance if the
                                // user taps the already-selected tab.
                                launchSingleTop = true
                                // restoreState re-applies the previously saved state for this
                                // tab (scroll position, ViewModel, etc.) when navigating back.
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },

        // ── FLOATING ACTION BUTTON ──
        // A prominent "+" button that lets the user quickly add a new meal. Tapping it calls
        // onNavigateToAddMeal, which navigates in the ROOT NavHost to the Add Meal screen
        // (outside the scaffold), so the bottom bar disappears while adding a meal.
        // The FAB is only shown on the Home tab; on all other tabs it is hidden.
        // This is driven by `currentRoute`, which recomposes whenever the user switches tabs.
        floatingActionButton = {
            if (currentRoute == Route.Home.route) {
                FloatingActionButton(
                    onClick = onNavigateToAddMeal,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add meal")
                }
            }
        }
    ) { padding ->
        // ── INNER NAV HOST ──
        // This NavHost is scoped to the scaffold's content area and uses innerNav (not the
        // root navController). It only knows about the five tab destinations.
        // Modifier.padding(padding) insets the content below the top bar and above the bottom
        // bar so nothing is hidden behind them.
        NavHost(
            navController = innerNav,
            startDestination = Route.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            // ── HOME TAB ──
            // viewModel() with a custom Factory is how we do manual constructor injection.
            // The Factory receives dependencies from `app` (the DI container) and passes
            // them to the ViewModel's constructor. Compose caches the ViewModel for the
            // lifetime of this NavBackStackEntry, so it survives recompositions and config
            // changes but is cleared when the user navigates away permanently.
            composable(Route.Home.route) {
                val homeVm: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        app.sessionStore,            // knows which user is logged in
                        app.database.mealEntryDao()  // reads today's meal entries
                    )
                )
                HomeScreen(viewModel = homeVm)
            }

            // ── LOG TAB ──
            composable(Route.Log.route) {
                val logVm: LogViewModel = viewModel(
                    factory = LogViewModel.Factory(
                        app.sessionStore,
                        app.database.mealEntryDao()
                    )
                )
                // LogScreen also receives onNavigateToAddMeal so it can offer an "add" action
                // that navigates to the Add Meal screen via the root NavHost.
                LogScreen(
                    viewModel = logVm,
                    onNavigateToAddMeal = onNavigateToAddMeal
                )
            }

            // ── SHOPPING TAB ──
            composable(Route.Shopping.route) {
                val shoppingVm: ShoppingViewModel = viewModel(
                    factory = ShoppingViewModel.Factory(
                        app.sessionStore,
                        app.database.shoppingDao()
                    )
                )
                ShoppingScreen(viewModel = shoppingVm)
            }

            // ── WATER TAB ──
            composable(Route.Water.route) {
                val waterVm: WaterViewModel = viewModel(
                    factory = WaterViewModel.Factory(
                        app.sessionStore,
                        app.database.waterDao()
                    )
                )
                WaterScreen(viewModel = waterVm)
            }

            // ── PLANS TAB ──
            // PlansViewModel needs more DAOs because it reads plan templates, meal templates,
            // and writes meal entries when the user applies a plan.
            composable(Route.Plans.route) {
                val plansVm: PlansViewModel = viewModel(
                    factory = PlansViewModel.Factory(
                        app.sessionStore,
                        app.database.planDao(),
                        app.database.mealTemplateDao(),
                        app.database.mealEntryDao()
                    )
                )
                PlansScreen(viewModel = plansVm)
            }
        }
    }
}
