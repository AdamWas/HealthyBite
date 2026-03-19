package pl.akp.healthybite

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.datastore.sessionDataStore
import pl.akp.healthybite.data.repository.FirestoreAuthRepository
import pl.akp.healthybite.data.repository.FirestoreMealRepository
import pl.akp.healthybite.data.repository.FirestorePlanRepository
import pl.akp.healthybite.data.repository.FirestoreSeeder
import pl.akp.healthybite.data.repository.FirestoreShoppingRepository
import pl.akp.healthybite.data.repository.FirestoreWaterRepository
import pl.akp.healthybite.domain.repository.AuthRepository
import pl.akp.healthybite.domain.repository.MealRepository
import pl.akp.healthybite.domain.repository.PlanRepository
import pl.akp.healthybite.domain.repository.ShoppingRepository
import pl.akp.healthybite.domain.repository.WaterRepository

class HealthyBiteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            Log.w(
                TAG,
                "No default FirebaseApp — add google-services.json to app/ and sync Gradle."
            )
        }
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val sessionStore: SessionDataStore by lazy {
        SessionDataStore(sessionDataStore)
    }

    val authRepository: AuthRepository by lazy {
        FirestoreAuthRepository(firestore, sessionStore)
    }

    val mealRepository: MealRepository by lazy {
        FirestoreMealRepository(firestore)
    }

    val shoppingRepository: ShoppingRepository by lazy {
        FirestoreShoppingRepository(firestore)
    }

    val waterRepository: WaterRepository by lazy {
        FirestoreWaterRepository(firestore)
    }

    val planRepository: PlanRepository by lazy {
        FirestorePlanRepository(firestore)
    }

    val firestoreSeeder: FirestoreSeeder by lazy {
        FirestoreSeeder(firestore)
    }

    private companion object {
        private const val TAG = "HealthyBiteApp"
    }
}
