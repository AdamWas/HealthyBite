package pl.akp.healthybite.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.akp.healthybite.domain.model.WaterEntry
import pl.akp.healthybite.domain.repository.WaterRepository

class WaterRepositoryImpl : WaterRepository {

    override fun getEntries(): Flow<List<WaterEntry>> {
        return flowOf(emptyList())
    }

    override suspend fun addEntry(entry: WaterEntry) {
        // Not implemented yet
    }
}

