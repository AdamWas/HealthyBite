package pl.akp.healthybite.domain.repository

import pl.akp.healthybite.domain.model.User

class EmailAlreadyExistsException : Exception("Email already in use")

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
    suspend fun logout()
}
