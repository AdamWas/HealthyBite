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

data class PlanWithItems(
    @Embedded val plan: PlanTemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "planTemplateId"
    )
    val items: List<PlanTemplateItemEntity>
)

@Dao
interface PlanDao {

    @Transaction
    @Query("SELECT * FROM plan_templates")
    fun observePlansWithItems(): Flow<List<PlanWithItems>>

    @Query("SELECT * FROM plan_templates")
    fun observePlans(): Flow<List<PlanTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: PlanTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PlanTemplateItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItems(items: List<PlanTemplateItemEntity>)
}
