package pl.akp.healthybite.ui.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val date: String = "",
    val entriesCount: Int = 0,
    val totalCalories: Int = 0,
    val totalProteinG: Int = 0,
    val totalFatG: Int = 0,
    val totalCarbsG: Int = 0,
    val errorMessage: String? = null
)
