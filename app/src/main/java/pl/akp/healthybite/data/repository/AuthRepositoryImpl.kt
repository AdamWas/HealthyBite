package pl.akp.healthybite.data.repository

import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.db.dao.UserDao
import pl.akp.healthybite.data.db.entity.UserEntity
import pl.akp.healthybite.domain.model.User
import pl.akp.healthybite.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        val entity = userDao.getByEmail(email)
            ?: return Result.failure(IllegalArgumentException("Invalid email or password"))

        if (entity.password != password) {
            return Result.failure(IllegalArgumentException("Invalid email or password"))
        }

        sessionDataStore.setLoggedIn(entity.id)
        return Result.success(entity.toDomain())
    }

    override suspend fun register(email: String, password: String) {
        // Not implemented yet
    }

    override suspend fun logout() {
        sessionDataStore.clear()
    }
}

private fun UserEntity.toDomain() = User(
    id = id,
    email = email,
    displayName = displayName
)
