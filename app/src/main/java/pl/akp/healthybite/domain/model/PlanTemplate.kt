package pl.akp.healthybite.domain.model

data class PlanTemplateItem(
    val mealTemplateName: String = "",
    val mealType: MealType = MealType.BREAKFAST
)

data class PlanTemplate(
    val id: String = "",
    val name: String = "",
    val items: List<PlanTemplateItem> = emptyList()
)
