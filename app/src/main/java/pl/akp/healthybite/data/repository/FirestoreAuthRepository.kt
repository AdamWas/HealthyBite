package pl.akp.healthybite.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.firebase.FirestoreDebug
import pl.akp.healthybite.data.firebase.getIntCoerced
import pl.akp.healthybite.data.firebase.getStringCoerced
import pl.akp.healthybite.domain.model.User
import pl.akp.healthybite.domain.repository.AuthRepository
import pl.akp.healthybite.domain.repository.EmailAlreadyExistsException

class FirestoreAuthRepository(
    private val firestore: FirebaseFirestore,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull()
                ?: return Result.failure(IllegalArgumentException("Invalid email or password"))

            val storedPassword = doc.getStringCoerced("password") ?: ""
            if (storedPassword != password) {
                return Result.failure(IllegalArgumentException("Invalid email or password"))
            }

            val user = doc.toUser()
            sessionDataStore.setLoggedIn(user.id)
            Result.success(user)
        } catch (e: Exception) {
            FirestoreDebug.log("login", e)
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<User> {
        return try {
            val existing = usersCollection
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (existing.documents.isNotEmpty()) {
                return Result.failure(EmailAlreadyExistsException())
            }

            val data = hashMapOf(
                "email" to email,
                "password" to password,
                "displayName" to null,
                "dailyCaloriesGoal" to 2000,
                "weightKg" to 0
            )
            val docRef = usersCollection.add(data).await()
            val user = User(
                id = docRef.id,
                email = email,
                displayName = null,
                dailyCaloriesGoal = 2000
            )
            sessionDataStore.setLoggedIn(docRef.id)
            Result.success(user)
        } catch (e: EmailAlreadyExistsException) {
            Result.failure(e)
        } catch (e: Exception) {
            FirestoreDebug.log("register", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionDataStore.clearSession()
    }

    override suspend fun getUser(userId: String): User? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) doc.toUser() else null
        } catch (e: Exception) {
            FirestoreDebug.log("getUser($userId)", e)
            null
        }
    }

    override suspend fun updateCaloriesGoal(userId: String, goal: Int) {
        try {
            usersCollection.document(userId)
                .update("dailyCaloriesGoal", goal)
                .await()
        } catch (e: Exception) {
            FirestoreDebug.log("updateCaloriesGoal($userId)", e)
            throw e
        }
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User {
    return User(
        id = id,
        email = getStringCoerced("email") ?: "",
        displayName = getStringCoerced("displayName"),
        dailyCaloriesGoal = getIntCoerced("dailyCaloriesGoal", 2000)
    )
}
