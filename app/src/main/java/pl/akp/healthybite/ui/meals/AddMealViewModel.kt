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
import pl.akp.healthybite.domain.model.MealEntry
import pl.akp.healthybite.domain.model.MealType
import pl.akp.healthybite.domain.repository.MealRepository
import java.time.LocalDate

class AddMealViewModel(
    private val sessionDataStore: SessionDataStore,
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMealUiState())
    val uiState: StateFlow<AddMealUiState> = _uiState.asStateFlow()

    init {
        observeTemplates()
    }

    private fun observeTemplates() {
        viewModelScope.launch {
            mealRepository.observeTemplatesByType(_uiState.value.selectedType)
                .collectLatest { list ->
                    _uiState.update { it.copy(templates = list, isLoading = false) }
                }
        }
    }

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

    fun onTemplateSelected(templateId: String) {
        _uiState.update { it.copy(selectedTemplateId = templateId) }
    }

    fun onCustomNameChanged(value: String) {
        _uiState.update {
            it.copy(
                customName = value,
                customNameError = if (value.isBlank()) "Name is required" else null
            )
        }
    }

    fun onCustomCaloriesChanged(value: String) {
        _uiState.update { it.copy(customCalories = value, caloriesError = validateInt(value, required = true)) }
    }

    fun onCustomProteinChanged(value: String) {
        _uiState.update { it.copy(customProtein = value, proteinError = validateInt(value, required = false)) }
    }

    fun onCustomFatChanged(value: String) {
        _uiState.update { it.copy(customFat = value, fatError = validateInt(value, required = false)) }
    }

    fun onCustomCarbsChanged(value: String) {
        _uiState.update { it.copy(customCarbs = value, carbsError = validateInt(value, required = false)) }
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            val userId = sessionDataStore.currentUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "Not logged in. Please sign in again.") }
                return@launch
            }

            val state = _uiState.value
            val today = LocalDate.now().toString()
            val now = System.currentTimeMillis()

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

    private suspend fun saveFromTemplate(userId: String, date: String, timestamp: Long, state: AddMealUiState) {
        val template = state.templates.firstOrNull { it.id == state.selectedTemplateId }
        if (template == null) {
            _uiState.update { it.copy(isSaving = false, errorMessage = "Select a template first") }
            return
        }
        mealRepository.insertEntry(
            MealEntry(
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

    private suspend fun saveCustom(userId: String, date: String, timestamp: Long, state: AddMealUiState) {
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

        mealRepository.insertEntry(
            MealEntry(
                userId = userId,
                name = state.customName.trim(),
                mealType = state.selectedType,
                date = date,
                timestamp = timestamp,
                kcal = state.customCalories.toInt(),
                proteinG = state.customProtein.toIntOrNull() ?: 0,
                fatG = state.customFat.toIntOrNull() ?: 0,
                carbsG = state.customCarbs.toIntOrNull() ?: 0
            )
        )
        _uiState.update { it.copy(isSaving = false, saved = true) }
    }

    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val mealRepository: MealRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddMealViewModel(sessionDataStore, mealRepository) as T
    }
}

private fun validateInt(value: String, required: Boolean): String? {
    if (value.isBlank()) return if (required) "Required" else null
    val n = value.toIntOrNull() ?: return "Must be a number"
    if (n < 0) return "Must be ≥ 0"
    return null
}
