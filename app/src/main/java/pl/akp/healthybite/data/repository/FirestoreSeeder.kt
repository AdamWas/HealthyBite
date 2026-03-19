package pl.akp.healthybite.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pl.akp.healthybite.BuildConfig
import pl.akp.healthybite.data.firebase.FirestoreDebug
import pl.akp.healthybite.domain.model.MealType

class FirestoreSeeder(
    private val firestore: FirebaseFirestore
) {

    suspend fun seedIfNeeded() {
        try {
            val metaDoc = firestore.collection("metadata").document("seedStatus")
            val snapshot = metaDoc.get().await()
            logStartupSnapshot("metadata/seedStatus", snapshot)
            if (snapshot.exists()) return

            seedDemoUser()
            seedMealTemplates()
            seedPlanTemplates()
            seedShoppingItems()

            metaDoc.set(
                mapOf(
                    "seeded" to true,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            FirestoreDebug.log("FirestoreSeeder.seedIfNeeded (aborted, will retry next launch)", e)
        }
    }

    private suspend fun seedDemoUser() {
        val existing = firestore.collection("users")
            .whereEqualTo("email", "demo@healthy.pl")
            .limit(1)
            .get()
            .await()

        if (existing.documents.isNotEmpty()) {
            existing.documents.forEach { logStartupSnapshot("users(query demo)", it) }
            return
        }

        firestore.collection("users").add(
            mapOf(
                "email" to "demo@healthy.pl",
                "password" to "zaq1@WSX",
                "displayName" to "Demo User",
                "dailyCaloriesGoal" to 2000,
                "weightKg" to 78
            )
        ).await()
    }

    private suspend fun seedMealTemplates() {
        val batch = firestore.batch()
        val col = firestore.collection("mealTemplates")

        val templates = listOf(
            mt("Oatmeal with fruits", MealType.BREAKFAST, 420, 18, 9, 65),
            mt("Scrambled eggs with avocado toast", MealType.BREAKFAST, 520, 28, 32, 30),
            mt("Protein pancakes", MealType.BREAKFAST, 480, 35, 12, 55),
            mt("Greek yogurt + granola", MealType.BREAKFAST, 390, 22, 14, 42),
            mt("Chicken + rice + veggies", MealType.LUNCH, 650, 45, 14, 80),
            mt("Salmon + quinoa + broccoli", MealType.LUNCH, 720, 42, 30, 55),
            mt("Turkey meatballs", MealType.LUNCH, 680, 50, 20, 60),
            mt("Veggie buddha bowl", MealType.LUNCH, 600, 22, 18, 85),
            mt("Chicken salad", MealType.DINNER, 450, 38, 18, 25),
            mt("Wholegrain sandwiches", MealType.DINNER, 400, 25, 12, 50),
            mt("Veggie omelette", MealType.DINNER, 350, 30, 20, 10),
            mt("Cottage cheese with chives", MealType.DINNER, 300, 28, 10, 20),
            mt("Apple + almonds", MealType.SNACK, 220, 6, 14, 20),
            mt("Protein bar", MealType.SNACK, 250, 20, 8, 23),
            mt("Banana + peanut butter", MealType.SNACK, 300, 8, 16, 35),
            mt("Fruit smoothie", MealType.SNACK, 280, 10, 5, 50),
        )

        templates.forEach { data ->
            batch.set(col.document(), data)
        }
        batch.commit().await()
    }

    private suspend fun seedPlanTemplates() {
        val col = firestore.collection("planTemplates")

        val plans = listOf(
            mapOf(
                "name" to "Cutting (~1800 kcal)",
                "items" to listOf(
                    pi("Oatmeal with fruits", MealType.BREAKFAST),
                    pi("Chicken + rice + veggies", MealType.LUNCH),
                    pi("Chicken salad", MealType.DINNER),
                    pi("Apple + almonds", MealType.SNACK),
                )
            ),
            mapOf(
                "name" to "High Protein (~2200 kcal)",
                "items" to listOf(
                    pi("Protein pancakes", MealType.BREAKFAST),
                    pi("Turkey meatballs", MealType.LUNCH),
                    pi("Cottage cheese with chives", MealType.DINNER),
                    pi("Protein bar", MealType.SNACK),
                )
            ),
            mapOf(
                "name" to "Veggie (~1900 kcal)",
                "items" to listOf(
                    pi("Greek yogurt + granola", MealType.BREAKFAST),
                    pi("Veggie buddha bowl", MealType.LUNCH),
                    pi("Veggie omelette", MealType.DINNER),
                    pi("Fruit smoothie", MealType.SNACK),
                )
            ),
        )

        val batch = firestore.batch()
        plans.forEach { data ->
            batch.set(col.document(), data)
        }
        batch.commit().await()
    }

    private suspend fun seedShoppingItems() {
        val demoUserSnapshot = firestore.collection("users")
            .whereEqualTo("email", "demo@healthy.pl")
            .limit(1)
            .get()
            .await()

        val demoDoc = demoUserSnapshot.documents.firstOrNull()
        demoDoc?.let { logStartupSnapshot("users(demo for shopping)", it) }
        val demoUserId = demoDoc?.id ?: return

        val items = listOf(
            "Oats" to "500 g",
            "Chicken breast" to "1 kg",
            "Basmati rice" to "1 kg",
            "Salmon" to "2 fillets",
            "Broccoli" to "2 pcs",
            "Eggs" to "10 pcs",
            "Greek yogurt" to "3 pcs",
            "Almonds" to "200 g",
            "Bananas" to "6 pcs",
        )

        val batch = firestore.batch()
        val col = firestore.collection("shoppingItems")
        items.forEach { (name, qty) ->
            batch.set(
                col.document(),
                mapOf(
                    "userId" to demoUserId,
                    "name" to name,
                    "quantity" to qty,
                    "isChecked" to false
                )
            )
        }
        batch.commit().await()
    }

    private fun mt(
        name: String, type: MealType,
        kcal: Int, protein: Int, fat: Int, carbs: Int
    ): Map<String, Any> = mapOf(
        "name" to name,
        "type" to type.name,
        "kcal" to kcal,
        "proteinG" to protein,
        "fatG" to fat,
        "carbsG" to carbs
    )

    private fun pi(mealName: String, mealType: MealType): Map<String, String> = mapOf(
        "mealTemplateName" to mealName,
        "mealType" to mealType.name
    )

    private fun logStartupSnapshot(context: String, doc: DocumentSnapshot) {
        if (!BuildConfig.DEBUG) return
        try {
            Log.d(TAG, "$context id=${doc.id} exists=${doc.exists()}")
            doc.data?.forEach { (name, value) ->
                Log.d(
                    TAG,
                    "  field=$name type=${value?.javaClass?.canonicalName} value=$value"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "logStartupSnapshot failed for $context", e)
        }
    }

    private companion object {
        private const val TAG = "HealthyBiteSeed"
    }
}
