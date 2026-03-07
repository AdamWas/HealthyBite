package pl.akp.healthybite.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.data.db.entity.MealEntryEntity

/**
 * Data-access object for the `meal_entries` table.
 *
 * Entries are the user's actual daily food log. They reference a [MealTemplateEntity]
 * when created from a template, or store standalone data for custom meals.
 */
@Dao
interface MealEntryDao {

    /**
     * Returns a reactive Flow of meal entries for the given user and date.
     * Because this returns a Flow, Room automatically re-emits the list whenever
     * the meal_entries table changes – the UI updates without manual refresh.
     * Used by LogViewModel and HomeViewModel to display today's food log.
     */
    @Query("SELECT * FROM meal_entries WHERE userId = :userId AND date = :date")
    fun observeEntriesForUserAndDate(userId: Long, date: String): Flow<List<MealEntryEntity>>

    /** Inserts a single meal entry; called from AddMealViewModel when the user logs a meal. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MealEntryEntity): Long

    /**
     * Bulk insert used when applying a meal plan to today's log.
     * PlansViewModel converts each plan item into a MealEntryEntity and passes them here.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<MealEntryEntity>)

    /** Removes a single meal entry by ID; called from LogScreen's delete action. */
    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
