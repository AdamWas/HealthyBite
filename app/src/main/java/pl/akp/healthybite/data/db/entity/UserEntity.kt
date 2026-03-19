package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a registered user in the local Room database.
 *
 * The `email` column has a unique index to prevent duplicate registrations.
 * Passwords are stored in plaintext (acceptable for a local-only demo app).
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // Auto-incremented primary key, used as the session user ID
    val email: String,                                   // Unique login identifier (enforced by the index above)
    val password: String,                                // Plaintext password (local-only demo; no server)
    val displayName: String?,                            // Optional display name shown on the Profile screen
    val dailyCaloriesGoal: Int = 2000,                   // User's target daily calories, editable in Profile
    val weightKg: Int = 0                                // User's weight in kilograms, shown in Profile
)
