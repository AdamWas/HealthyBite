package pl.akp.healthybite.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.data.db.entity.ShoppingItemEntity

/**
 * Data-access object for the `shopping_items` table.
 *
 * Items are scoped to a user. The default ordering places unchecked items
 * first (sorted by newest) so the active list stays at the top.
 */
@Dao
interface ShoppingDao {

    /**
     * Reactive list of shopping items for the given user.
     * ORDER BY isChecked ASC puts unchecked (0/false) items above checked (1/true),
     * and id DESC sorts newest items first within each group.
     * Used by ShoppingViewModel to drive the Shopping screen's list.
     */
    @Query("SELECT * FROM shopping_items WHERE userId = :userId ORDER BY isChecked ASC, id DESC")
    fun observeItemsByUser(userId: Long): Flow<List<ShoppingItemEntity>>

    /** Inserts a new shopping item; called from ShoppingViewModel when the user adds an item. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity): Long

    /** Bulk insert used by DatabaseSeeder for demo shopping items. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    /**
     * Toggles the checked/unchecked state of a shopping item.
     * Called from ShoppingViewModel when the user taps the checkbox on an item.
     */
    @Query("UPDATE shopping_items SET isChecked = :checked WHERE id = :id")
    suspend fun updateChecked(id: Long, checked: Boolean)

    /** Removes a shopping item from the list; called from ShoppingScreen's delete action. */
    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
