package pl.akp.healthybite.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.akp.healthybite.domain.model.ShoppingItem
import pl.akp.healthybite.domain.repository.ShoppingRepository

class ShoppingRepositoryImpl : ShoppingRepository {

    override fun getItems(): Flow<List<ShoppingItem>> {
        return flowOf(emptyList())
    }

    override suspend fun addItem(item: ShoppingItem) {
        // Not implemented yet
    }

    override suspend fun toggleItemChecked(id: Long) {
        // Not implemented yet
    }
}

