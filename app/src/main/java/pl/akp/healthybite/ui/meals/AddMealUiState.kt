package pl.akp.healthybite.ui.meals

import pl.akp.healthybite.domain.model.MealTemplate
import pl.akp.healthybite.domain.model.MealType

enum class AddMealMode { TEMPLATE, CUSTOM }

data class AddMealUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedType: MealType = MealType.BREAKFAST,
    val templates: List<MealTemplate> = emptyList(),
    val mode: AddMealMode = AddMealMode.TEMPLATE,
    val selectedTemplateId: String? = null,
    val customName: String = "",
    val customCalories: String = "",
    val customProtein: String = "",
    val customFat: String = "",
    val customCarbs: String = "",
    val customNameError: String? = null,
    val caloriesError: String? = null,
    val proteinError: String? = null,
    val fatError: String? = null,
    val carbsError: String? = null,
    val errorMessage: String? = null,
    val saved: Boolean = false
) {
    val submitEnabled: Boolean
        get() = when (mode) {
            AddMealMode.TEMPLATE -> selectedTemplateId != null && !isSaving
            AddMealMode.CUSTOM -> customName.isNotBlank()
                    && customCalories.isNotBlank()
                    && customNameError == null
                    && caloriesError == null
                    && proteinError == null
                    && fatError == null
                    && carbsError == null
                    && !isSaving
        }
}
