package pl.akp.healthybite.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.akp.healthybite.data.db.entity.UserEntity

/** Data-access object for the `users` table – handles auth lookups and profile updates. */
@Dao
interface UserDao {

    /** Called by AuthRepositoryImpl.login() to look up a user by email during authentication. */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    /** Called by AuthRepositoryImpl.getUser() and SplashViewModel to verify a persisted session. */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    /**
     * Inserts a new user; aborts if the unique email constraint is violated.
     * Called by AuthRepositoryImpl.register() – returns the auto-generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    /**
     * Inserts a user only if no row with the same primary key exists (used for seeding).
     * Called by DatabaseSeeder.seedDemoUser() to avoid duplicating the demo account.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(user: UserEntity): Long

    /** Called by ProfileViewModel when the user edits their daily calorie goal. */
    @Query("UPDATE users SET dailyCaloriesGoal = :goal WHERE id = :userId")
    suspend fun updateCaloriesGoal(userId: Long, goal: Int)
}
