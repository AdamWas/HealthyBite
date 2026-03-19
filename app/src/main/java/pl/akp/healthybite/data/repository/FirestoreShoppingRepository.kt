package pl.akp.healthybite.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pl.akp.healthybite.data.firebase.FirestoreDebug
import pl.akp.healthybite.data.firebase.asFirestoreBoolean
import pl.akp.healthybite.data.firebase.getRawField
import pl.akp.healthybite.data.firebase.getStringCoerced
import pl.akp.healthybite.domain.model.ShoppingItem
import pl.akp.healthybite.domain.repository.ShoppingRepository

class FirestoreShoppingRepository(
    private val firestore: FirebaseFirestore
) : ShoppingRepository {

    private val collection = firestore.collection("shoppingItems")

    override fun observeItemsByUser(userId: String): Flow<List<ShoppingItem>> =
        callbackFlow {
            val registration = collection
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        FirestoreDebug.log("observeItemsByUser", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents?.mapNotNull { doc ->
                        doc.toShoppingItem()
                    }?.sortedWith(compareBy<ShoppingItem> { it.isChecked }.thenByDescending { it.id })
                        ?: emptyList()
                    trySend(items)
                }
            awaitClose { registration.remove() }
        }

    override suspend fun insertItem(item: ShoppingItem): String {
        val data = mapOf(
            "userId" to item.userId,
            "name" to item.name,
            "quantity" to item.quantity,
            "isChecked" to item.isChecked
        )
        val docRef = collection.add(data).await()
        return docRef.id
    }

    override suspend fun updateChecked(itemId: String, checked: Boolean) {
        collection.document(itemId).update("isChecked", checked).await()
    }

    override suspend fun deleteItem(itemId: String) {
        collection.document(itemId).delete().await()
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toShoppingItem(): ShoppingItem? {
    return try {
        ShoppingItem(
            id = id,
            userId = getStringCoerced("userId") ?: return null,
            name = getStringCoerced("name") ?: return null,
            quantity = getStringCoerced("quantity") ?: "",
            isChecked = getRawField("isChecked").asFirestoreBoolean(false)
        )
    } catch (e: Exception) {
        FirestoreDebug.log("toShoppingItem($id)", e)
        null
    }
}
