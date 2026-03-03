package pl.akp.healthybite.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.data.db.entity.MealTemplateEntity

@Dao
interface MealTemplateDao {

    @Query("SELECT * FROM meal_templates")
    fun observeTemplates(): Flow<List<MealTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: MealTemplateEntity)
}

