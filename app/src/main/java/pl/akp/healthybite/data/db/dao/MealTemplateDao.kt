package pl.akp.healthybite.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.data.db.entity.MealTemplateEntity
import pl.akp.healthybite.domain.model.MealType

@Dao
interface MealTemplateDao {

    @Query("SELECT * FROM meal_templates")
    fun observeTemplates(): Flow<List<MealTemplateEntity>>

    @Query("SELECT * FROM meal_templates WHERE type = :type")
    fun observeByType(type: MealType): Flow<List<MealTemplateEntity>>

    @Query("SELECT * FROM meal_templates WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): MealTemplateEntity?

    @Query("SELECT COUNT(*) FROM meal_templates")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: MealTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(templates: List<MealTemplateEntity>)
}
