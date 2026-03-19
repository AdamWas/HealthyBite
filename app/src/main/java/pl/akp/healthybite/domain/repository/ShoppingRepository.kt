package pl.akp.healthybite.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.domain.model.ShoppingItem

interface ShoppingRepository {
    fun observeItemsByUser(userId: String): Flow<List<ShoppingItem>>
    suspend fun insertItem(item: ShoppingItem): String
    suspend fun updateChecked(itemId: String, checked: Boolean)
    suspend fun deleteItem(itemId: String)
}
