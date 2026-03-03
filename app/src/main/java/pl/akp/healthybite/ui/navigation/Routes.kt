package pl.akp.healthybite.ui.navigation

sealed class Route(val route: String) {
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")
    data object Log : Route("log")
    data object AddMeal : Route("add_meal")
    data object Shopping : Route("shopping")
    data object Water : Route("water")
    data object Plans : Route("plans")
    data object Profile : Route("profile")
}

