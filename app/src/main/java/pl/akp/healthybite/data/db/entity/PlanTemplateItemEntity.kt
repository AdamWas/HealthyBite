package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.akp.healthybite.domain.model.MealType

/**
 * A single meal slot within a [PlanTemplateEntity].
 *
 * References the meal template by [mealTemplateName] (rather than by ID) so that
 * the plan stays human-readable in the database and is resilient to template re-seeding.
 */
@Entity(tableName = "plan_template_items")
data class PlanTemplateItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planTemplateId: Long,       // Foreign key to plan_templates.id – links this item to its parent plan
    val mealTemplateName: String,   // References a MealTemplateEntity by name (resolved at plan-apply time via getByName)
    val mealType: MealType          // Category (Breakfast/Lunch/Dinner/Snack) for display grouping within the plan
)
