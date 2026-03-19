package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.akp.healthybite.domain.model.MealType

/**
 * A single logged meal for a user on a specific [date] (ISO-8601 string).
 *
 * When created from a template, [templateId] links back to the source
 * [MealTemplateEntity]; for custom meals it defaults to 0.
 * Macros (kcal, protein, fat, carbs) are denormalised from the template
 * at insertion time so the log stays accurate even if templates change later.
 */
@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // Auto-incremented primary key
    val userId: Long,                                    // Foreign key to users.id – scopes entries to the logged-in user
    val templateId: Long = 0,                            // Links back to meal_templates.id (0 for custom meals)
    val name: String,                                    // Display name of the logged meal
    val mealType: MealType = MealType.BREAKFAST,         // Category (Breakfast/Lunch/Dinner/Snack), stored as String via EnumConverters
    val date: String,                                    // ISO-8601 date string (e.g. "2026-03-07") for grouping entries by day
    val timestamp: Long,                                 // Epoch millis when the entry was created, used for ordering
    val kcal: Int = 0,                                   // Denormalised calories from the template at insertion time
    val proteinG: Int = 0,                               // Denormalised protein grams
    val fatG: Int = 0,                                   // Denormalised fat grams
    val carbsG: Int = 0,                                 // Denormalised carbohydrate grams
    val notes: String? = null                            // Optional free-text notes the user can attach to an entry
)
