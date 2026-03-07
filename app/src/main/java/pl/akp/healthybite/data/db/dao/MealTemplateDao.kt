package pl.akp.healthybite.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.data.db.entity.MealTemplateEntity
import pl.akp.healthybite.domain.model.MealType

/**
 * Data-access object for the `meal_templates` table.
 *
 * Templates are reusable meal definitions (name, kcal, macros) grouped by [MealType].
 * They are shown on the "Add Meal → From Templates" screen and referenced
 * by [PlanTemplateItemEntity] when applying a meal plan.
 */
@Dao
interface MealTemplateDao {

    /**
     * Reactive Flow filtered by meal type – used on the Add Meal screen.
     * When the user selects a tab (e.g. Breakfast), AddMealViewModel collects this
     * Flow to display only templates of that category. Auto-updates if templates change.
     */
    @Query("SELECT * FROM meal_templates WHERE type = :type")
    fun observeByType(type: MealType): Flow<List<MealTemplateEntity>>

    /**
     * One-shot lookup by name – used when applying a plan to resolve template macros.
     * PlansViewModel calls this to find the full nutritional data for each plan item
     * (plan items reference templates by name, not by ID).
     */
    @Query("SELECT * FROM meal_templates WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): MealTemplateEntity?

    /**
     * Quick row-count check used by DatabaseSeeder.seedIfNeeded() as the guard condition.
     * If count() > 0, the seeder skips insertion (data already exists).
     */
    @Query("SELECT COUNT(*) FROM meal_templates")
    suspend fun count(): Int

    /** Inserts or replaces a single template (used if a custom template feature is added). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: MealTemplateEntity)

    /** Bulk insert with IGNORE strategy – used by DatabaseSeeder to insert demo templates. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(templates: List<MealTemplateEntity>)
}
