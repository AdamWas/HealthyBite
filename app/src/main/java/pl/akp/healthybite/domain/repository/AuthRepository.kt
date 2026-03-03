package pl.akp.healthybite.domain.repository

import pl.akp.healthybite.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String)
    suspend fun logout()
}
