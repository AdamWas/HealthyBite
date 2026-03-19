package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.akp.healthybite.domain.model.MealType

/**
 * A reusable meal definition with predefined nutritional values.
 *
 * Templates are categorised by [MealType] (Breakfast, Lunch, Dinner, Snack)
 * and shown as selectable options on the "Add Meal" screen.
 */
@Entity(tableName = "meal_templates")
data class MealTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // Auto-incremented primary key
    val name: String,                                    // Human-readable meal name (e.g. "Oatmeal with fruits")
    val type: MealType,                                  // Category enum stored as String via EnumConverters
    val kcal: Int = 0,                                   // Calorie count for this template
    val proteinG: Int = 0,                               // Protein in grams
    val fatG: Int = 0,                                   // Fat in grams
    val carbsG: Int = 0                                  // Carbohydrates in grams
)
