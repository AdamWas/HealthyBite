package pl.akp.healthybite.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.data.db.entity.WaterEntryEntity

@Dao
interface WaterDao {

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE userId = :userId AND date = :date")
    fun observeTotalForDate(userId: Long, date: String): Flow<Int?>

    @Query("SELECT * FROM water_entries WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun observeEntriesForDate(userId: Long, date: String): Flow<List<WaterEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaterEntryEntity): Long
}
