package pl.akp.healthybite.domain.model

data class MealTemplate(
    val id: Long,
    val name: String,
    val type: MealType
)

