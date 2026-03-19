package pl.akp.healthybite.ui.plans

import pl.akp.healthybite.domain.model.MealType

/**
 * Represents a single meal inside a plan card (e.g. "Breakfast → Oatmeal").
 * Used purely for display — the actual template resolution happens in the ViewModel.
 */
data class PlanItemUi(
    /** Which meal slot this item belongs to (BREAKFAST, LUNCH, DINNER, SNACK). */
    val mealType: MealType,
    /** Human-readable meal template name shown next to the meal-type badge. */
    val mealName: String
)

/**
 * Presentation model for one plan card in the Plans tab.
 * Created by [PlansViewModel.toCardModel] which resolves each plan item's
 * template to compute the aggregate calorie count for display.
 */
data class PlanCardModel(
    /** Database ID used as the LazyColumn key and to identify the plan on "Apply". */
    val planId: Long,
    /** Plan display name shown in the card header (e.g. "High-protein day"). */
    val name: String,
    /** Ordered list of meals that make up this plan. */
    val items: List<PlanItemUi>,
    /** Sum of kcal across all resolved templates; null if templates couldn't be resolved. */
    val totalKcal: Int? = null
)

/**
 * UI state for the Plans tab.
 *
 * [applyingPlanId] tracks which plan's "Apply to today" button is currently
 * in-progress, disabling all other apply buttons to prevent double-taps.
 */
data class PlansUiState(
    /** True while the plan list is being loaded from Room for the first time. */
    val isLoading: Boolean = true,
    /** All available plans, each pre-resolved with total kcal for display. */
    val plans: List<PlanCardModel> = emptyList(),
    /**
     * When non-null, indicates which plan's "Apply to today" action is in progress.
     * While set, all apply buttons are disabled to prevent duplicate inserts.
     */
    val applyingPlanId: Long? = null,
    /** Error message shown via snackbar (e.g. "Apply failed: …"). */
    val errorMessage: String? = null,
    /** Success message shown via snackbar (e.g. "Plan applied to today (3 meals)"). */
    val successMessage: String? = null
)
