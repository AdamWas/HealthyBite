package pl.akp.healthybite.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.domain.model.WaterEntry

interface WaterRepository {
    fun getEntries(): Flow<List<WaterEntry>>
    suspend fun addEntry(entry: WaterEntry)
}

