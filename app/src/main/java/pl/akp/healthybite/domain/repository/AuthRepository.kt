package pl.akp.healthybite.domain.repository

import pl.akp.healthybite.domain.model.User

/**
 * Thrown by [AuthRepository.register] when the email is already taken.
 * AuthRepositoryImpl checks for an existing row before inserting; if one is
 * found it wraps this exception in Result.failure so RegisterViewModel can
 * surface an appropriate error message on the Register screen.
 */
class EmailAlreadyExistsException : Exception("Email already in use")

/**
 * Domain-layer contract for authentication and user-profile operations.
 *
 * Implemented by [AuthRepositoryImpl] in the data layer.
 * Using an interface keeps ViewModels testable without a real database.
 */
interface AuthRepository {

    /**
     * Authenticates with email + password.
     * Called by AuthViewModel on the Login screen.
     * Returns Result.success(User) on match, or Result.failure on bad credentials.
     */
    suspend fun login(email: String, password: String): Result<User>

    /**
     * Creates a new account after validating uniqueness.
     * Called by RegisterViewModel on the Register screen.
     * Returns Result.success(User) on success, or Result.failure wrapping
     * EmailAlreadyExistsException if the email is already in use.
     */
    suspend fun register(email: String, password: String): Result<User>

    /**
     * Clears the current session (userId + email) from SessionDataStore.
     * Called by ProfileViewModel when the user taps "Log out".
     */
    suspend fun logout()

    /**
     * Fetches a User by primary key.
     * Called by HomeViewModel and ProfileViewModel to hydrate the current session.
     * Returns null if the id does not exist (e.g. stale session after DB wipe).
     */
    suspend fun getUser(userId: Long): User?

    /**
     * Persists a new daily calorie goal for the given user.
     * Called by ProfileViewModel when the user updates their target on the Profile screen.
     */
    suspend fun updateCaloriesGoal(userId: Long, goal: Int)
}
