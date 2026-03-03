package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_template_items")
data class PlanTemplateItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planTemplateId: Long,
    val mealTemplateId: Long
)

