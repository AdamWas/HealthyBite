package pl.akp.healthybite.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single item on a user's shopping list.
 *
 * [isChecked] tracks whether the item has been purchased; checked items
 * sort to the bottom of the list in the UI.
 */
@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // Auto-incremented primary key
    val userId: Long,                                    // Foreign key to users.id – scopes items to a specific user
    val name: String,                                    // Item name displayed in the shopping list (e.g. "Chicken breast")
    val quantity: String = "",                           // Human-readable quantity (e.g. "1 kg", "6 pcs"); empty if unspecified
    val isChecked: Boolean = false                       // True when the user has marked the item as purchased
)
