package pl.akp.healthybite.ui.profile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val email: String = "",
    val caloriesGoal: String = "",
    val caloriesGoalError: String? = null,
    val saving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val logoutSuccess: Boolean = false,
    val noSession: Boolean = false
)
