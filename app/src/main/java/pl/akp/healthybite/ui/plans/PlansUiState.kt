package pl.akp.healthybite.ui.plans

import pl.akp.healthybite.domain.model.MealType

data class PlanItemUi(
    val mealType: MealType,
    val mealName: String
)

data class PlanCardModel(
    val planId: String,
    val name: String,
    val items: List<PlanItemUi>,
    val totalKcal: Int? = null
)

data class PlansUiState(
    val isLoading: Boolean = true,
    val plans: List<PlanCardModel> = emptyList(),
    val applyingPlanId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
