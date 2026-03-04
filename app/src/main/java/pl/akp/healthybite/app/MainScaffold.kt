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

private data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(Route.Home.route, Icons.Filled.Home, "Home"),
    BottomNavItem(Route.Log.route, Icons.Filled.MenuBook, "Log"),
    BottomNavItem(Route.Shopping.route, Icons.Filled.ShoppingCart, "Shopping"),
    BottomNavItem(Route.Water.route, Icons.Filled.WaterDrop, "Water"),
    BottomNavItem(Route.Plans.route, Icons.Filled.CalendarMonth, "Plans"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    onNavigateToProfile: () -> Unit,
    onNavigateToAddMeal: () -> Unit
) {
    val app = LocalContext.current.applicationContext as HealthyBiteApplication
    val innerNav = rememberNavController()
    val backStackEntry by innerNav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
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
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            innerNav.navigate(item.route) {
                                popUpTo(innerNav.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMeal,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add meal")
            }
        }
    ) { padding ->
        NavHost(
            navController = innerNav,
            startDestination = Route.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.Home.route) {
                val homeVm: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        app.sessionStore,
                        app.database.mealEntryDao()
                    )
                )
                HomeScreen(viewModel = homeVm)
            }
            composable(Route.Log.route) {
                val logVm: LogViewModel = viewModel(
                    factory = LogViewModel.Factory(
                        app.sessionStore,
                        app.database.mealEntryDao()
                    )
                )
                LogScreen(
                    viewModel = logVm,
                    onNavigateToAddMeal = onNavigateToAddMeal
                )
            }
            composable(Route.Shopping.route) {
                val shoppingVm: ShoppingViewModel = viewModel(
                    factory = ShoppingViewModel.Factory(
                        app.sessionStore,
                        app.database.shoppingDao()
                    )
                )
                ShoppingScreen(viewModel = shoppingVm)
            }
            composable(Route.Water.route) {
                val waterVm: WaterViewModel = viewModel(
                    factory = WaterViewModel.Factory(
                        app.sessionStore,
                        app.database.waterDao()
                    )
                )
                WaterScreen(viewModel = waterVm)
            }
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
