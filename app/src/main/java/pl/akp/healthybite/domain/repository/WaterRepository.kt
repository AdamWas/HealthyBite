package pl.akp.healthybite.domain.repository

import kotlinx.coroutines.flow.Flow

interface WaterRepository {
    fun observeTotalForDate(userId: String, date: String): Flow<Int>
    suspend fun insertEntry(userId: String, date: String, amountMl: Int, timestamp: Long): String
}
