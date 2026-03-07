package pl.akp.healthybite.domain.model

/**
 * Domain model representing an authenticated user.
 *
 * This is the UI-facing model mapped from [UserEntity] via [AuthRepositoryImpl.toDomain];
 * it intentionally omits sensitive fields like password.
 */
data class User(
    // Primary key from the users table; assigned by Room on insert
    val id: Long,
    // Unique email used for login; sourced from UserEntity.email
    val email: String,
    // Optional display name shown in the Profile screen; null when the user hasn't set one yet
    val displayName: String?,
    // Daily calorie target displayed on Home/Log screens; defaults to 2 000 kcal for new users
    val dailyCaloriesGoal: Int = 2000
)

