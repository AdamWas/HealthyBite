package pl.akp.healthybite.ui.meals

import pl.akp.healthybite.data.db.entity.MealTemplateEntity
import pl.akp.healthybite.domain.model.MealType

/**
 * Determines whether the user is adding a meal from a predefined template or entering custom data.
 *
 * TEMPLATE – the user picks from a list of prepopulated meal templates stored in the database.
 * CUSTOM   – the user manually enters a meal name, calorie count, and optional macro values.
 */
enum class AddMealMode { TEMPLATE, CUSTOM }

/** UI state for the Add Meal screen – covers both template selection and custom input modes. */
data class AddMealUiState(

    // --- Loading / saving state ---
    val isLoading: Boolean = true,       // true while the template list is being fetched from Room
    val isSaving: Boolean = false,       // true while a meal entry is being inserted into the database

    // --- Meal type selection (Breakfast / Lunch / Dinner / Snack) ---
    val selectedType: MealType = MealType.BREAKFAST, // drives which templates are shown

    // --- Template mode fields ---
    val templates: List<MealTemplateEntity> = emptyList(), // templates fetched for the selected MealType
    val mode: AddMealMode = AddMealMode.TEMPLATE,          // current mode toggle (Template vs Custom)
    val selectedTemplateId: Long? = null,                  // ID of the template the user tapped; null = none chosen yet

    // --- Custom mode form fields ---
    val customName: String = "",       // user-typed meal name
    val customCalories: String = "",   // user-typed calorie count (required)
    val customProtein: String = "",    // user-typed protein grams (optional)
    val customFat: String = "",        // user-typed fat grams (optional)
    val customCarbs: String = "",      // user-typed carbs grams (optional)

    // --- Per-field validation errors (null = no error) ---
    val customNameError: String? = null,
    val caloriesError: String? = null,
    val proteinError: String? = null,
    val fatError: String? = null,
    val carbsError: String? = null,

    // --- General error and save-complete flag ---
    val errorMessage: String? = null,  // non-field-specific error ("Not logged in", "Save failed", etc.)
    val saved: Boolean = false         // set to true after a successful insert; triggers back-navigation
) {
    /**
     * Computed property that the Save button binds to via `enabled = state.submitEnabled`.
     *
     * Template mode: only needs a selected template and no save in progress.
     * Custom mode: requires non-blank name and calories, zero validation errors, and no save in progress.
     */
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
