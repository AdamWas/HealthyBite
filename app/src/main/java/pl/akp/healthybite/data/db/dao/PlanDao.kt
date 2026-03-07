package pl.akp.healthybite.data.db.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.data.db.entity.PlanTemplateEntity
import pl.akp.healthybite.data.db.entity.PlanTemplateItemEntity

/**
 * Room relation model that joins a [PlanTemplateEntity] with its child
 * [PlanTemplateItemEntity] rows in a single transactional query.
 *
 * @Embedded inlines all PlanTemplateEntity columns into this class.
 * @Relation tells Room to automatically fetch matching PlanTemplateItemEntity rows
 * where plan_template_items.planTemplateId == plan_templates.id.
 */
data class PlanWithItems(
    @Embedded val plan: PlanTemplateEntity,
    @Relation(
        parentColumn = "id",           // PlanTemplateEntity.id
        entityColumn = "planTemplateId" // PlanTemplateItemEntity.planTemplateId
    )
    val items: List<PlanTemplateItemEntity>
)

/**
 * Data-access object for meal plans (`plan_templates` + `plan_template_items`).
 *
 * Plans are predefined collections of meals (e.g. "Cutting ~1800 kcal") that
 * users can apply to today's log with one tap.
 */
@Dao
interface PlanDao {

    /**
     * Observes all plans together with their meal items (1-to-many relation).
     *
     * @Transaction ensures Room fetches parent rows AND child rows in a single
     * consistent snapshot (no partial reads if another coroutine writes mid-query).
     * Returns a reactive Flow used by PlansViewModel to display the plans list.
     */
    @Transaction
    @Query("SELECT * FROM plan_templates")
    fun observePlansWithItems(): Flow<List<PlanWithItems>>

    /** Inserts a plan template and returns its auto-generated ID (used by DatabaseSeeder). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: PlanTemplateEntity): Long

    /** Bulk-inserts plan items linking meals to a plan (used by DatabaseSeeder). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItems(items: List<PlanTemplateItemEntity>)
}
