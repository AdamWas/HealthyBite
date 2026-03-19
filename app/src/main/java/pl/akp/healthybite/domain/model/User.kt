package pl.akp.healthybite.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val dailyCaloriesGoal: Int = 2000
)
