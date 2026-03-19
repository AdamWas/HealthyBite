package pl.akp.healthybite.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pl.akp.healthybite.data.firebase.FirestoreDebug
import pl.akp.healthybite.data.firebase.asFirestoreInt
import pl.akp.healthybite.data.firebase.getRawField
import pl.akp.healthybite.domain.repository.WaterRepository

class FirestoreWaterRepository(
    private val firestore: FirebaseFirestore
) : WaterRepository {

    private val collection = firestore.collection("waterEntries")

    override fun observeTotalForDate(userId: String, date: String): Flow<Int> =
        callbackFlow {
            val registration = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        FirestoreDebug.log("observeTotalForDate", error)
                        trySend(0)
                        return@addSnapshotListener
                    }
                    val total = snapshot?.documents?.sumOf { doc ->
                        doc.getRawField("amountMl").asFirestoreInt() ?: 0
                    } ?: 0
                    trySend(total)
                }
            awaitClose { registration.remove() }
        }

    override suspend fun insertEntry(
        userId: String,
        date: String,
        amountMl: Int,
        timestamp: Long
    ): String {
        val data = mapOf(
            "userId" to userId,
            "date" to date,
            "amountMl" to amountMl,
            "timestamp" to timestamp
        )
        val docRef = collection.add(data).await()
        return docRef.id
    }
}
