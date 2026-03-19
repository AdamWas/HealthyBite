package pl.akp.healthybite.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import pl.akp.healthybite.data.firebase.FirestoreDebug
import pl.akp.healthybite.data.firebase.asFirestoreString
import pl.akp.healthybite.data.firebase.asMealTypeOrNull
import pl.akp.healthybite.data.firebase.getRawField
import pl.akp.healthybite.data.firebase.getStringCoerced
import pl.akp.healthybite.data.firebase.logFirestoreMalformedItem
import pl.akp.healthybite.domain.model.PlanTemplate
import pl.akp.healthybite.domain.model.PlanTemplateItem
import pl.akp.healthybite.domain.repository.PlanRepository

class FirestorePlanRepository(
    private val firestore: FirebaseFirestore
) : PlanRepository {

    private val collection = firestore.collection("planTemplates")

    override fun observePlans(): Flow<List<PlanTemplate>> =
        callbackFlow {
            val registration = collection
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        FirestoreDebug.log("observePlans", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val plans = snapshot?.documents?.mapNotNull { doc ->
                        doc.toPlanTemplate()
                    } ?: emptyList()
                    trySend(plans)
                }
            awaitClose { registration.remove() }
        }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toPlanTemplate(): PlanTemplate? {
    return try {
        val name = getStringCoerced("name") ?: return null
        val itemsRaw = getRawField("items")
        val itemsList: List<*> = when (itemsRaw) {
            is List<*> -> itemsRaw
            null -> emptyList<Any>()
            else -> {
                FirestoreDebug.log(
                    "planTemplates/$id/items",
                    IllegalStateException("expected list, got ${itemsRaw.javaClass.name}")
                )
                emptyList<Any>()
            }
        }
        val items = itemsList.mapNotNull { raw ->
            val m = raw as? Map<*, *> ?: run {
                logFirestoreMalformedItem("planTemplates", id, "item is not a map: $raw")
                return@mapNotNull null
            }
            val mealName = m["mealTemplateName"].asFirestoreString() ?: run {
                logFirestoreMalformedItem("planTemplates", id, "missing mealTemplateName")
                return@mapNotNull null
            }
            val mealType = m["mealType"].asMealTypeOrNull() ?: run {
                logFirestoreMalformedItem("planTemplates", id, "bad mealType raw=${m["mealType"]}")
                return@mapNotNull null
            }
            PlanTemplateItem(mealTemplateName = mealName, mealType = mealType)
        }
        PlanTemplate(id = id, name = name, items = items)
    } catch (e: Exception) {
        FirestoreDebug.log("toPlanTemplate($id)", e)
        null
    }
}
