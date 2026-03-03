package pl.akp.healthybite.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.domain.model.MealEntry
import pl.akp.healthybite.domain.model.MealTemplate

interface MealsRepository {
    fun getMealTemplates(): Flow<List<MealTemplate>>
    fun getMealEntries(): Flow<List<MealEntry>>
    suspend fun addMealEntry(entry: MealEntry)
}

