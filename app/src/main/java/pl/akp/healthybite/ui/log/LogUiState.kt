package pl.akp.healthybite.ui.log

import pl.akp.healthybite.domain.model.MealEntry

data class LogUiState(
    val isLoading: Boolean = true,
    val date: String = "",
    val entries: List<MealEntry> = emptyList(),
    val totalCalories: Int = 0,
    val totalProteinG: Int = 0,
    val totalFatG: Int = 0,
    val totalCarbsG: Int = 0,
    val errorMessage: String? = null
)
