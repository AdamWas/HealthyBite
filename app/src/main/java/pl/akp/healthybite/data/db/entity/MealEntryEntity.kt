package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.akp.healthybite.domain.model.MealType

@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val templateId: Long = 0,
    val name: String,
    val mealType: MealType = MealType.BREAKFAST,
    val date: String,
    val timestamp: Long,
    val kcal: Int = 0,
    val proteinG: Int = 0,
    val fatG: Int = 0,
    val carbsG: Int = 0,
    val notes: String? = null
)
