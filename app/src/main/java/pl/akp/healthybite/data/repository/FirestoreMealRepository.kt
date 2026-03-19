package pl.akp.healthybite.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pl.akp.healthybite.data.firebase.FirestoreDebug
import pl.akp.healthybite.data.firebase.asMealTypeOrNull
import pl.akp.healthybite.data.firebase.getIntCoerced
import pl.akp.healthybite.data.firebase.getLongCoerced
import pl.akp.healthybite.data.firebase.getRawField
import pl.akp.healthybite.data.firebase.getStringCoerced
import pl.akp.healthybite.domain.model.MealEntry
import pl.akp.healthybite.domain.model.MealTemplate
import pl.akp.healthybite.domain.model.MealType
import pl.akp.healthybite.domain.repository.MealRepository

class FirestoreMealRepository(
    private val firestore: FirebaseFirestore
) : MealRepository {

    private val entriesCollection = firestore.collection("mealEntries")
    private val templatesCollection = firestore.collection("mealTemplates")

    override fun observeEntriesForUserAndDate(userId: String, date: String): Flow<List<MealEntry>> =
        callbackFlow {
            val registration = entriesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        FirestoreDebug.log("observeEntriesForUserAndDate", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val entries = snapshot?.documents?.mapNotNull { doc ->
                        doc.toMealEntry()
                    } ?: emptyList()
                    trySend(entries)
                }
            awaitClose { registration.remove() }
        }

    override fun observeTemplatesByType(type: MealType): Flow<List<MealTemplate>> =
        callbackFlow {
            val registration = templatesCollection
                .whereEqualTo("type", type.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        FirestoreDebug.log("observeTemplatesByType", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val templates = snapshot?.documents?.mapNotNull { doc ->
                        doc.toMealTemplate()
                    }?.sortedBy { it.name } ?: emptyList()
                    trySend(templates)
                }
            awaitClose { registration.remove() }
        }

    override suspend fun getTemplateByName(name: String): MealTemplate? {
        return try {
            val snapshot = templatesCollection
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toMealTemplate()
        } catch (e: Exception) {
            FirestoreDebug.log("getTemplateByName($name)", e)
            null
        }
    }

    override suspend fun insertEntry(entry: MealEntry): String {
        return try {
            val data = entry.toMap()
            val docRef = entriesCollection.add(data).await()
            docRef.id
        } catch (e: Exception) {
            FirestoreDebug.log("insertEntry", e)
            throw e
        }
    }

    override suspend fun insertAllEntries(entries: List<MealEntry>) {
        try {
            val batch = firestore.batch()
            entries.forEach { entry ->
                val docRef = entriesCollection.document()
                batch.set(docRef, entry.toMap())
            }
            batch.commit().await()
        } catch (e: Exception) {
            FirestoreDebug.log("insertAllEntries", e)
            throw e
        }
    }

    override suspend fun deleteEntry(entryId: String) {
        try {
            entriesCollection.document(entryId).delete().await()
        } catch (e: Exception) {
            FirestoreDebug.log("deleteEntry($entryId)", e)
            throw e
        }
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toMealEntry(): MealEntry? {
    return try {
        MealEntry(
            id = id,
            userId = getStringCoerced("userId") ?: return null,
            templateId = getStringCoerced("templateId") ?: "",
            name = getStringCoerced("name") ?: return null,
            mealType = getRawField("mealType").asMealTypeOrNull() ?: MealType.BREAKFAST,
            date = getStringCoerced("date") ?: "",
            timestamp = getLongCoerced("timestamp", 0L),
            kcal = getIntCoerced("kcal", 0),
            proteinG = getIntCoerced("proteinG", 0),
            fatG = getIntCoerced("fatG", 0),
            carbsG = getIntCoerced("carbsG", 0),
            notes = getStringCoerced("notes")
        )
    } catch (e: Exception) {
        FirestoreDebug.log("toMealEntry($id)", e)
        null
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toMealTemplate(): MealTemplate? {
    return try {
        MealTemplate(
            id = id,
            name = getStringCoerced("name") ?: return null,
            type = getRawField("type").asMealTypeOrNull() ?: MealType.BREAKFAST,
            kcal = getIntCoerced("kcal", 0),
            proteinG = getIntCoerced("proteinG", 0),
            fatG = getIntCoerced("fatG", 0),
            carbsG = getIntCoerced("carbsG", 0)
        )
    } catch (e: Exception) {
        FirestoreDebug.log("toMealTemplate($id)", e)
        null
    }
}

private fun MealEntry.toMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "templateId" to templateId,
    "name" to name,
    "mealType" to mealType.name,
    "date" to date,
    "timestamp" to timestamp,
    "kcal" to kcal,
    "proteinG" to proteinG,
    "fatG" to fatG,
    "carbsG" to carbsG,
    "notes" to notes
)
