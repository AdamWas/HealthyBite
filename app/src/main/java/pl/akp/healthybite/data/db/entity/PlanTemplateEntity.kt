package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_templates")
data class PlanTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

