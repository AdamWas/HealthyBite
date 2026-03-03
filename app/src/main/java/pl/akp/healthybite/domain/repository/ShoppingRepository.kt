package pl.akp.healthybite.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.domain.model.ShoppingItem

interface ShoppingRepository {
    fun getItems(): Flow<List<ShoppingItem>>
    suspend fun addItem(item: ShoppingItem)
    suspend fun toggleItemChecked(id: Long)
}

