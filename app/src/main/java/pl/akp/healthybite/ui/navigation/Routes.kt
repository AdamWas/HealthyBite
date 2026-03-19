package pl.akp.healthybite.ui.navigation

/**
 * Type-safe route definitions for both the root [NavGraph] and the
 * inner bottom-nav [NavHost] inside [MainScaffold].
 *
 * Splash / Login / Register / Main / Profile / AddMeal live in the root NavHost.
 * Home / Log / Shopping / Water / Plans live in the inner (tab) NavHost.
 */
sealed class Route(val route: String) {

    // ── Root NavHost destinations ──────────────────────────────────────
    // These screens are managed by the root NavHostController in NavGraph.

    /** Initial loading screen; checks for an existing session then redirects. */
    data object Splash : Route("splash")

    /** Email/password login form; entry point when no session is found. */
    data object Login : Route("login")

    /** New-account registration form; reachable from the Login screen. */
    data object Register : Route("register")

    /**
     * Container destination that hosts [MainScaffold].
     * MainScaffold owns a *second* inner NavHost for the bottom-navigation tabs.
     */
    data object Main : Route("main")

    /** Full-screen profile/settings; pushed on top of Main in the root NavHost. */
    data object Profile : Route("profile")

    /** Add-meal form; pushed on top of Main and pops back on save/cancel. */
    data object AddMeal : Route("add_meal")

    // ── Inner (tab) NavHost destinations ───────────────────────────────
    // These screens live inside MainScaffold's own NavHost and correspond
    // to the bottom-navigation tabs. They are NOT in the root NavHost.

    /** Dashboard / daily summary tab. */
    data object Home : Route("home")

    /** Meal-log / diary tab showing today's logged meals. */
    data object Log : Route("log")

    /** Shopping-list tab for groceries tied to the current plan. */
    data object Shopping : Route("shopping")

    /** Water-intake tracking tab. */
    data object Water : Route("water")

    /** Meal-plan browser/editor tab. */
    data object Plans : Route("plans")
}
