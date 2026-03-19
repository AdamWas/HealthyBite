package pl.akp.healthybite.ui.profile

/**
 * UI state for the Profile screen.
 *
 * [noSession] triggers automatic navigation back to Login when the
 * DataStore has no user ID (e.g. after a destructive DB migration).
 */
data class ProfileUiState(
    val isLoading: Boolean = true,            // true while the user profile is being fetched from the repository
    val email: String = "",                   // the logged-in user's email, displayed in the AccountCard
    val caloriesGoal: String = "",            // current text in the calorie-goal input field (String for TextField binding)
    val caloriesGoalError: String? = null,    // validation error for the goal field (non-numeric, out of 1000-5000 range)
    val saving: Boolean = false,              // true while the updated goal is being persisted — disables the Save button
    val errorMessage: String? = null,         // general error message shown via Snackbar (e.g. "Failed to load profile")
    val successMessage: String? = null,       // success message shown via Snackbar after saving the goal
    val logoutSuccess: Boolean = false,       // set to true after logout completes — triggers navigation to Login
    val noSession: Boolean = false            // true when DataStore has no userId (edge case, e.g. after DB migration)
)
