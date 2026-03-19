package pl.akp.healthybite.domain.model

data class MealEntry(
    val id: String = "",
    val userId: String = "",
    val templateId: String = "",
    val name: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val date: String = "",
    val timestamp: Long = 0,
    val kcal: Int = 0,
    val proteinG: Int = 0,
    val fatG: Int = 0,
    val carbsG: Int = 0,
    val notes: String? = null
)
