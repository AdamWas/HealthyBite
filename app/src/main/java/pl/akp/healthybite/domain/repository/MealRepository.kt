package pl.akp.healthybite.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.domain.model.MealEntry
import pl.akp.healthybite.domain.model.MealTemplate
import pl.akp.healthybite.domain.model.MealType

interface MealRepository {
    fun observeEntriesForUserAndDate(userId: String, date: String): Flow<List<MealEntry>>
    fun observeTemplatesByType(type: MealType): Flow<List<MealTemplate>>
    suspend fun getTemplateByName(name: String): MealTemplate?
    suspend fun insertEntry(entry: MealEntry): String
    suspend fun insertAllEntries(entries: List<MealEntry>)
    suspend fun deleteEntry(entryId: String)
}
