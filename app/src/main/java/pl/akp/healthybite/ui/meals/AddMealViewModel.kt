package pl.akp.healthybite.ui.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.db.dao.MealEntryDao
import pl.akp.healthybite.data.db.dao.MealTemplateDao
import pl.akp.healthybite.data.db.entity.MealEntryEntity
import pl.akp.healthybite.domain.model.MealType
import java.time.LocalDate

/**
 * ViewModel for the Add Meal screen.
 *
 * Supports two modes:
 * - **Template**: observes [MealTemplateDao] filtered by the selected [MealType],
 *   lets the user pick a template, and inserts a [MealEntryEntity] with its macros.
 * - **Custom**: validates user-entered name/calories/macros and inserts a manual entry.
 *
 * After a successful save, sets [AddMealUiState.saved] to trigger back-navigation.
 */
class AddMealViewModel(
    private val sessionDataStore: SessionDataStore,
    private val mealTemplateDao: MealTemplateDao,
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMealUiState())
    val uiState: StateFlow<AddMealUiState> = _uiState.asStateFlow()

    init {
        // Kick off the first template observation as soon as the ViewModel is created.
        observeTemplates()
    }

    /**
     * Subscribes to the Room Flow of templates filtered by the currently selected MealType.
     * Each call launches a new coroutine (collectLatest), so changing the type means
     * calling this again — the new collector supersedes the old one thanks to collectLatest.
     */
    private fun observeTemplates() {
        viewModelScope.launch {
            mealTemplateDao.observeByType(_uiState.value.selectedType)
                .collectLatest { list ->
                    _uiState.update { it.copy(templates = list, isLoading = false) }
                }
        }
    }

    /**
     * Called when the user taps a different meal type segment (e.g. Lunch → Dinner).
     * Resets the previously selected template (no longer valid for the new type),
     * sets loading, and re-subscribes to templates for the new type.
     */
    fun onTypeSelected(type: MealType) {
        _uiState.update {
            it.copy(
                selectedType = type,
                selectedTemplateId = null,
                isLoading = true
            )
        }
        observeTemplates()
    }

    /**
     * Called when the user taps the Template / Custom filter chip.
     * Clears all validation errors so the user starts the new mode with a clean slate.
     */
    fun onModeChanged(mode: AddMealMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                errorMessage = null,
                customNameError = null,
                caloriesError = null,
                proteinError = null,
                fatError = null,
                carbsError = null
            )
        }
    }

    /** Stores the ID of the template the user tapped in the list; enables the Save button. */
    fun onTemplateSelected(templateId: Long) {
        _uiState.update { it.copy(selectedTemplateId = templateId) }
    }

    /** Updates the custom meal name and immediately validates (blank → error). */
    fun onCustomNameChanged(value: String) {
        _uiState.update {
            it.copy(
                customName = value,
                customNameError = if (value.isBlank()) "Name is required" else null
            )
        }
    }

    /** Updates calories text and validates via validateInt (required field). */
    fun onCustomCaloriesChanged(value: String) {
        _uiState.update { it.copy(customCalories = value, caloriesError = validateInt(value, required = true)) }
    }

    /** Updates protein text and validates via validateInt (optional — blank is OK). */
    fun onCustomProteinChanged(value: String) {
        _uiState.update { it.copy(customProtein = value, proteinError = validateInt(value, required = false)) }
    }

    /** Updates fat text and validates via validateInt (optional — blank is OK). */
    fun onCustomFatChanged(value: String) {
        _uiState.update { it.copy(customFat = value, fatError = validateInt(value, required = false)) }
    }

    /** Updates carbs text and validates via validateInt (optional — blank is OK). */
    fun onCustomCarbsChanged(value: String) {
        _uiState.update { it.copy(customCarbs = value, carbsError = validateInt(value, required = false)) }
    }

    /**
     * Entry point for saving a meal, triggered by the Save button.
     *
     * 1. Reads the current userId from the DataStore (guards against no session).
     * 2. Captures today's date and a timestamp for the entry.
     * 3. Delegates to [saveFromTemplate] or [saveCustom] depending on the active mode.
     * 4. On success, [AddMealUiState.saved] is set to true, which the UI observes
     *    via LaunchedEffect to trigger back-navigation.
     */
    fun onSaveClicked() {
        viewModelScope.launch {
            val userId = sessionDataStore.currentUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "Not logged in. Please sign in again.") }
                return@launch
            }

            val state = _uiState.value
            val today = LocalDate.now().toString() // ISO date string used as the partition key in the DB
            val now = System.currentTimeMillis()   // epoch millis for ordering entries within a day

            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                when (state.mode) {
                    AddMealMode.TEMPLATE -> saveFromTemplate(userId, today, now, state)
                    AddMealMode.CUSTOM -> saveCustom(userId, today, now, state)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Save failed: ${e.message}") }
            }
        }
    }

    /**
     * Saves a meal entry by copying nutritional data from the selected template.
     * Looks up the template in the already-loaded list (avoids an extra DB query),
     * builds a [MealEntryEntity] with the template's macros, and inserts it.
     * The templateId foreign key is stored so the entry can be traced back to its source.
     */
    private suspend fun saveFromTemplate(userId: Long, date: String, timestamp: Long, state: AddMealUiState) {
        val template = state.templates.firstOrNull { it.id == state.selectedTemplateId }
        if (template == null) {
            _uiState.update { it.copy(isSaving = false, errorMessage = "Select a template first") }
            return
        }
        mealEntryDao.insert(
            MealEntryEntity(
                userId = userId,
                templateId = template.id,
                name = template.name,
                mealType = template.type,
                date = date,
                timestamp = timestamp,
                kcal = template.kcal,
                proteinG = template.proteinG,
                fatG = template.fatG,
                carbsG = template.carbsG
            )
        )
        _uiState.update { it.copy(isSaving = false, saved = true) }
    }

    /**
     * Saves a meal entry from custom (user-typed) data.
     *
     * Re-validates every field before inserting — this is a final guard in case the user
     * bypassed the real-time per-keystroke validation somehow. If any field has an error,
     * the errors are shown and the insert is skipped.
     *
     * Optional macro fields (protein, fat, carbs) default to 0 when left blank.
     */
    private suspend fun saveCustom(userId: Long, date: String, timestamp: Long, state: AddMealUiState) {
        // Final validation pass across all custom fields
        val nameErr = if (state.customName.isBlank()) "Name is required" else null
        val calErr = validateInt(state.customCalories, required = true)
        val proErr = validateInt(state.customProtein, required = false)
        val fatErr = validateInt(state.customFat, required = false)
        val carbErr = validateInt(state.customCarbs, required = false)

        if (nameErr != null || calErr != null || proErr != null || fatErr != null || carbErr != null) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    customNameError = nameErr,
                    caloriesError = calErr,
                    proteinError = proErr,
                    fatError = fatErr,
                    carbsError = carbErr
                )
            }
            return
        }

        mealEntryDao.insert(
            MealEntryEntity(
                userId = userId,
                name = state.customName.trim(),
                mealType = state.selectedType,
                date = date,
                timestamp = timestamp,
                kcal = state.customCalories.toInt(),
                proteinG = state.customProtein.toIntOrNull() ?: 0, // blank → 0
                fatG = state.customFat.toIntOrNull() ?: 0,         // blank → 0
                carbsG = state.customCarbs.toIntOrNull() ?: 0      // blank → 0
            )
        )
        _uiState.update { it.copy(isSaving = false, saved = true) }
    }

    /**
     * Manual dependency-injection factory.
     * Used because the project does not use Hilt/Dagger; the NavGraph creates this Factory
     * with instances obtained from the Application-level dependency container.
     */
    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val mealTemplateDao: MealTemplateDao,
        private val mealEntryDao: MealEntryDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddMealViewModel(sessionDataStore, mealTemplateDao, mealEntryDao) as T
    }
}

/**
 * Validates a numeric text field; returns an error message or null if valid.
 *
 * Checks in order: blank (error only if [required]), non-integer, negative value.
 */
private fun validateInt(value: String, required: Boolean): String? {
    if (value.isBlank()) return if (required) "Required" else null
    val n = value.toIntOrNull() ?: return "Must be a number"
    if (n < 0) return "Must be ≥ 0"
    return null
}
