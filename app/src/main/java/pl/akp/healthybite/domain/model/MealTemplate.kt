package pl.akp.healthybite.domain.model

data class MealTemplate(
    val id: String = "",
    val name: String = "",
    val type: MealType = MealType.BREAKFAST,
    val kcal: Int = 0,
    val proteinG: Int = 0,
    val fatG: Int = 0,
    val carbsG: Int = 0
)
