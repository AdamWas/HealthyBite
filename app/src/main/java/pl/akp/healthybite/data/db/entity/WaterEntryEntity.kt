package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records a single water intake event (e.g. +250 ml) for a given user and date.
 *
 * The total daily intake is computed by summing [amountMl] across all entries
 * for a user/date pair via [WaterDao.observeTotalForDate].
 */
@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // Auto-incremented primary key
    val userId: Long,                                    // Foreign key to users.id – scopes entries to the logged-in user
    val date: String,                                    // ISO-8601 date string (e.g. "2026-03-07") for daily grouping
    val amountMl: Int,                                   // Water intake in millilitres for this single event
    val timestamp: Long                                  // Epoch millis when the entry was recorded, used for ordering
)
