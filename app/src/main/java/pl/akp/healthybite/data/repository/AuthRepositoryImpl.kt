package pl.akp.healthybite.data.repository

import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.db.dao.UserDao
import pl.akp.healthybite.data.db.entity.UserEntity
import pl.akp.healthybite.domain.model.User
import pl.akp.healthybite.domain.repository.AuthRepository
import pl.akp.healthybite.domain.repository.EmailAlreadyExistsException

/**
 * Concrete [AuthRepository] backed by Room ([UserDao]) and [SessionDataStore].
 *
 * Login/register results are wrapped in [Result] so the ViewModel can
 * distinguish success from specific failure types (wrong password, duplicate email)
 * without try/catch.
 */
class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    /**
     * Authenticates a user by email + password comparison.
     * On success, persists the session in DataStore.
     */
    override suspend fun login(email: String, password: String): Result<User> {
        // Step 1: Look up the user row by email. Returns null if no account exists.
        val entity = userDao.getByEmail(email)
            ?: return Result.failure(IllegalArgumentException("Invalid email or password"))

        // Step 2: Compare the plaintext password (acceptable for local-only demo).
        if (entity.password != password) {
            return Result.failure(IllegalArgumentException("Invalid email or password"))
        }

        // Step 3: Persist the session so the user stays logged in across app restarts.
        sessionDataStore.setLoggedIn(entity.id)
        return Result.success(entity.toDomain())
    }

    /**
     * Creates a new user account.
     * Fails with [EmailAlreadyExistsException] if the email is already taken.
     */
    override suspend fun register(email: String, password: String): Result<User> {
        // Step 1: Check for duplicate email before attempting insertion.
        val existing = userDao.getByEmail(email)
        if (existing != null) {
            return Result.failure(EmailAlreadyExistsException())
        }

        // Step 2: Insert the new user row; Room returns the auto-generated primary key.
        val entity = UserEntity(email = email, password = password, displayName = null)
        val id = userDao.insert(entity)

        // Step 3: Immediately log the new user in by persisting the session.
        sessionDataStore.setLoggedIn(id)
        return Result.success(User(id = id, email = email, displayName = null))
    }

    /**
     * Clears the session in DataStore (no server-side session to invalidate).
     * After this call, currentUserId emits null and isLoggedIn emits false,
     * which triggers navigation back to the Login screen.
     */
    override suspend fun logout() {
        sessionDataStore.clearSession()
    }

    /**
     * Fetches a user from the Room database and maps the entity to the domain model.
     * Returns null if the userId doesn't match any row (e.g. after a destructive migration).
     */
    override suspend fun getUser(userId: Long): User? {
        return userDao.getById(userId)?.toDomain()
    }

    override suspend fun updateCaloriesGoal(userId: Long, goal: Int) {
        userDao.updateCaloriesGoal(userId, goal)
    }
}

/**
 * Maps the persistence [UserEntity] to the domain [User] model.
 *
 * Strips the password field so it never leaks into the UI / domain layer.
 * Only the fields that the UI needs (id, email, displayName, dailyCaloriesGoal) are kept.
 */
private fun UserEntity.toDomain() = User(
    id = id,
    email = email,
    displayName = displayName,
    dailyCaloriesGoal = dailyCaloriesGoal
)
