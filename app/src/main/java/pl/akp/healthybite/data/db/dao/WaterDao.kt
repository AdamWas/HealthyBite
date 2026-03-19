package pl.akp.healthybite.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.data.db.entity.WaterEntryEntity

/**
 * Data-access object for the `water_entries` table.
 *
 * Each row is a single water intake event (e.g. +250 ml). The DAO provides
 * both a summed total and a full entry list for a given user + date.
 */
@Dao
interface WaterDao {

    /**
     * Returns the SUM of amountMl for all water entries on the given user+date as a reactive Flow.
     * Emits null when there are no rows (no water logged yet). Room re-emits automatically
     * whenever a row is inserted/updated/deleted for this table.
     * Used by WaterViewModel and HomeViewModel to show today's total intake.
     */
    @Query("SELECT SUM(amountMl) FROM water_entries WHERE userId = :userId AND date = :date")
    fun observeTotalForDate(userId: Long, date: String): Flow<Int?>

    /** Reactive list of individual water entries, newest first. Used on the Water screen. */
    @Query("SELECT * FROM water_entries WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun observeEntriesForDate(userId: Long, date: String): Flow<List<WaterEntryEntity>>

    /** Inserts a new water intake event; called from WaterViewModel when the user adds water. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaterEntryEntity): Long
}
