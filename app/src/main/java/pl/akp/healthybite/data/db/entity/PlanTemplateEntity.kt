package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A named meal plan (e.g. "Cutting ~1800 kcal").
 *
 * The plan's actual meals are stored as child rows in [PlanTemplateItemEntity]
 * and joined via [PlanWithItems].
 */
@Entity(tableName = "plan_templates")
data class PlanTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // Auto-incremented primary key, referenced by PlanTemplateItemEntity
    val name: String                                     // Display name shown on the Plans screen (e.g. "Cutting (~1800 kcal)")
)

