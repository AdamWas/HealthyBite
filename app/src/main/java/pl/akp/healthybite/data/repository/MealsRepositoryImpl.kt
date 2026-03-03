package pl.akp.healthybite.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.akp.healthybite.domain.model.MealEntry
import pl.akp.healthybite.domain.model.MealTemplate
import pl.akp.healthybite.domain.repository.MealsRepository

class MealsRepositoryImpl : MealsRepository {

    override fun getMealTemplates(): Flow<List<MealTemplate>> {
        return flowOf(emptyList())
    }

    override fun getMealEntries(): Flow<List<MealEntry>> {
        return flowOf(emptyList())
    }

    override suspend fun addMealEntry(entry: MealEntry) {
        // Not implemented yet
    }
}

