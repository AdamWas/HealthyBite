package pl.akp.healthybite.ui.plans

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
import pl.akp.healthybite.data.db.dao.PlanDao
import pl.akp.healthybite.data.db.dao.PlanWithItems
import pl.akp.healthybite.data.db.entity.MealEntryEntity
import java.time.LocalDate

class PlansViewModel(
    private val sessionDataStore: SessionDataStore,
    private val planDao: PlanDao,
    private val mealTemplateDao: MealTemplateDao,
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlansUiState())
    val uiState: StateFlow<PlansUiState> = _uiState.asStateFlow()

    private var plansWithItems: List<PlanWithItems> = emptyList()

    init {
        observePlans()
    }

    private fun observePlans() {
        viewModelScope.launch {
            planDao.observePlansWithItems().collectLatest { list ->
                plansWithItems = list
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        plans = list.map { pw -> pw.toCardModel() }
                    )
                }
            }
        }
    }

    fun onApplyClicked(planId: Long) {
        viewModelScope.launch {
            val userId = sessionDataStore.currentUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "Please log in again") }
                return@launch
            }

            _uiState.update { it.copy(applyingPlanId = planId, errorMessage = null, successMessage = null) }

            try {
                val plan = plansWithItems.firstOrNull { it.plan.id == planId }
                if (plan == null) {
                    _uiState.update { it.copy(applyingPlanId = null, errorMessage = "Plan not found") }
                    return@launch
                }

                val today = LocalDate.now().toString()
                val now = System.currentTimeMillis()
                val entries = plan.items.mapNotNull { item ->
                    val template = mealTemplateDao.getByName(item.mealTemplateName) ?: return@mapNotNull null
                    MealEntryEntity(
                        userId = userId,
                        templateId = template.id,
                        name = template.name,
                        mealType = item.mealType,
                        date = today,
                        timestamp = now,
                        kcal = template.kcal,
                        proteinG = template.proteinG,
                        fatG = template.fatG,
                        carbsG = template.carbsG
                    )
                }

                if (entries.isEmpty()) {
                    _uiState.update { it.copy(applyingPlanId = null, errorMessage = "No matching templates found") }
                    return@launch
                }

                mealEntryDao.insertAll(entries)
                _uiState.update {
                    it.copy(
                        applyingPlanId = null,
                        successMessage = "Plan applied to today (${entries.size} meals)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(applyingPlanId = null, errorMessage = "Apply failed: ${e.message}") }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun PlanWithItems.toCardModel(): PlanCardModel {
        var totalKcal = 0
        val uiItems = items.map { item ->
            val template = mealTemplateDao.getByName(item.mealTemplateName)
            if (template != null) totalKcal += template.kcal
            PlanItemUi(mealType = item.mealType, mealName = item.mealTemplateName)
        }
        return PlanCardModel(
            planId = plan.id,
            name = plan.name,
            items = uiItems,
            totalKcal = totalKcal
        )
    }

    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val planDao: PlanDao,
        private val mealTemplateDao: MealTemplateDao,
        private val mealEntryDao: MealEntryDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlansViewModel(sessionDataStore, planDao, mealTemplateDao, mealEntryDao) as T
    }
}
