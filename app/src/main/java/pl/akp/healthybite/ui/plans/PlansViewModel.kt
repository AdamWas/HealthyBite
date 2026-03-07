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

/**
 * ViewModel for the Plans tab.
 *
 * Observes all meal plans (with items) from [PlanDao] and resolves each
 * plan item's template to compute total kcal. "Apply to today" resolves
 * each plan item → meal template → [MealEntryEntity] and bulk-inserts
 * them into today's log.
 */
class PlansViewModel(
    private val sessionDataStore: SessionDataStore,
    private val planDao: PlanDao,
    private val mealTemplateDao: MealTemplateDao,
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlansUiState())
    val uiState: StateFlow<PlansUiState> = _uiState.asStateFlow()

    /**
     * Caches the raw Room [PlanWithItems] data so [onApplyClicked] can look up
     * the full plan + items without re-querying the database.
     */
    private var plansWithItems: List<PlanWithItems> = emptyList()

    init {
        observePlans()
    }

    /**
     * Watches [PlanDao.observePlansWithItems] and maps each [PlanWithItems]
     * to a [PlanCardModel] (resolving template kcal for display).
     * The result is stored both in [plansWithItems] (raw cache) and in
     * [_uiState.plans] (UI-ready models).
     */
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

    /**
     * Applies the selected plan to today's meal log. Steps:
     *
     * 1. Retrieves the current userId; bails with an error if not logged in.
     * 2. Sets [PlansUiState.applyingPlanId] to disable all apply buttons and show a spinner.
     * 3. Finds the cached [PlanWithItems] matching [planId].
     * 4. For each plan item, resolves the meal template by name via [MealTemplateDao.getByName].
     *    Items whose template can't be found are silently skipped (mapNotNull).
     * 5. Creates a [MealEntryEntity] per resolved template, stamped with today's date.
     * 6. Bulk-inserts all entries via [MealEntryDao.insertAll].
     * 7. On success, shows a snackbar with the count of inserted meals.
     *    On failure, shows an error snackbar.
     * 8. Clears [applyingPlanId] in either case to re-enable the buttons.
     */
    fun onApplyClicked(planId: Long) {
        viewModelScope.launch {
            val userId = sessionDataStore.currentUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "Please log in again") }
                return@launch
            }

            // Mark this plan as "applying" to show spinner and lock all apply buttons
            _uiState.update { it.copy(applyingPlanId = planId, errorMessage = null, successMessage = null) }

            try {
                val plan = plansWithItems.firstOrNull { it.plan.id == planId }
                if (plan == null) {
                    _uiState.update { it.copy(applyingPlanId = null, errorMessage = "Plan not found") }
                    return@launch
                }

                val today = LocalDate.now().toString()
                val now = System.currentTimeMillis()

                // Resolve each plan item → meal template → MealEntryEntity
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

                // Bulk-insert all resolved entries into today's log
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

    /** Called by the UI after the success snackbar has been shown, to prevent re-showing. */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /** Called by the UI after the error snackbar has been shown, to prevent re-showing. */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Converts a Room [PlanWithItems] into a display-ready [PlanCardModel].
     *
     * For each plan item, looks up the corresponding [MealTemplateEntity] by name
     * and accumulates its kcal into [totalKcal]. If a template isn't found the
     * item still appears in the card but its calories aren't counted.
     */
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

    /**
     * Manual dependency injection factory.
     * Required because [PlansViewModel] takes multiple DAOs + a DataStore that
     * Android's default ViewModelProvider cannot supply on its own.
     */
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
