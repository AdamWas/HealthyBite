package pl.akp.healthybite.data.db

import androidx.room.withTransaction
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.akp.healthybite.data.db.entity.MealTemplateEntity
import pl.akp.healthybite.data.db.entity.PlanTemplateEntity
import pl.akp.healthybite.data.db.entity.PlanTemplateItemEntity
import pl.akp.healthybite.data.db.entity.ShoppingItemEntity
import pl.akp.healthybite.data.db.entity.UserEntity
import pl.akp.healthybite.domain.model.MealType

/**
 * DAO-level database seeder invoked on app startup (from [SplashViewModel]).
 *
 * Uses a double-checked locking pattern (volatile flag + [Mutex]) so seeding
 * runs at most once per app process, even under concurrent coroutine launches.
 * All seed inserts happen inside a single Room transaction for atomicity.
 */
class DatabaseSeeder(private val database: AppDatabase) {

    // Mutex serialises concurrent coroutine access so seeding cannot run twice.
    private val mutex = Mutex()

    // @Volatile ensures the flag is visible across threads immediately after write.
    // Together with the Mutex this forms a double-checked locking pattern:
    //   1st check (outside lock): fast path – skip if already done.
    //   2nd check (inside lock):  guarantees only one coroutine actually seeds.
    @Volatile private var done = false

    /**
     * Seeds demo data only when the meal_templates table is empty.
     *
     * Called by SplashViewModel.init{} on every app launch. The double-checked
     * locking plus the count() guard ensure seeding runs at most once per process
     * and is idempotent across app restarts (won't re-insert if data already exists).
     */
    suspend fun seedIfNeeded() {
        if (done) return                                    // fast path: already seeded this process
        mutex.withLock {
            if (done) return                                // re-check after acquiring the lock
            if (database.mealTemplateDao().count() > 0) {   // guard: table already has rows (e.g. from PrepopulateCallback)
                done = true
                return
            }
            // All seed inserts run inside a single Room transaction for atomicity –
            // if anything fails, no partial data is committed.
            database.withTransaction {
                seedDemoUser()
                seedMealTemplates()
                seedPlanTemplates()
                seedShoppingItems()
            }
            done = true
        }
    }

    /** Inserts a demo user account so the app can be explored without registering. */
    private suspend fun seedDemoUser() {
        database.userDao().insertIgnore(
            UserEntity(
                email = "demo@healthy.pl",
                password = "zaq1@WSX",
                displayName = "Demo User",
                dailyCaloriesGoal = 2000,
                weightKg = 78
            )
        )
    }

    /** Inserts 16 meal templates (4 per MealType) that populate the Add Meal screen. */
    private suspend fun seedMealTemplates() {
        database.mealTemplateDao().insertAll(
            listOf(
                meal("Oatmeal with fruits", MealType.BREAKFAST, 420, 18, 9, 65),
                meal("Scrambled eggs with avocado toast", MealType.BREAKFAST, 520, 28, 32, 30),
                meal("Protein pancakes", MealType.BREAKFAST, 480, 35, 12, 55),
                meal("Greek yogurt + granola", MealType.BREAKFAST, 390, 22, 14, 42),

                meal("Chicken + rice + veggies", MealType.LUNCH, 650, 45, 14, 80),
                meal("Salmon + quinoa + broccoli", MealType.LUNCH, 720, 42, 30, 55),
                meal("Turkey meatballs", MealType.LUNCH, 680, 50, 20, 60),
                meal("Veggie buddha bowl", MealType.LUNCH, 600, 22, 18, 85),

                meal("Chicken salad", MealType.DINNER, 450, 38, 18, 25),
                meal("Wholegrain sandwiches", MealType.DINNER, 400, 25, 12, 50),
                meal("Veggie omelette", MealType.DINNER, 350, 30, 20, 10),
                meal("Cottage cheese with chives", MealType.DINNER, 300, 28, 10, 20),

                meal("Apple + almonds", MealType.SNACK, 220, 6, 14, 20),
                meal("Protein bar", MealType.SNACK, 250, 20, 8, 23),
                meal("Banana + peanut butter", MealType.SNACK, 300, 8, 16, 35),
                meal("Fruit smoothie", MealType.SNACK, 280, 10, 5, 50),
            )
        )
    }

    /**
     * Inserts 3 plan templates and 12 plan items (4 meals per plan).
     * Each plan item references a meal template by name so it can be resolved later.
     */
    private suspend fun seedPlanTemplates() {
        val planDao = database.planDao()

        // Insert parent plan rows first; the returned IDs are used as foreign keys for items.
        val cuttingId = planDao.insert(PlanTemplateEntity(name = "Cutting (~1800 kcal)"))
        val highProteinId = planDao.insert(PlanTemplateEntity(name = "High Protein (~2200 kcal)"))
        val veggieId = planDao.insert(PlanTemplateEntity(name = "Veggie (~1900 kcal)"))

        planDao.insertItems(
            listOf(
                planItem(cuttingId, "Oatmeal with fruits", MealType.BREAKFAST),
                planItem(cuttingId, "Chicken + rice + veggies", MealType.LUNCH),
                planItem(cuttingId, "Chicken salad", MealType.DINNER),
                planItem(cuttingId, "Apple + almonds", MealType.SNACK),

                planItem(highProteinId, "Protein pancakes", MealType.BREAKFAST),
                planItem(highProteinId, "Turkey meatballs", MealType.LUNCH),
                planItem(highProteinId, "Cottage cheese with chives", MealType.DINNER),
                planItem(highProteinId, "Protein bar", MealType.SNACK),

                planItem(veggieId, "Greek yogurt + granola", MealType.BREAKFAST),
                planItem(veggieId, "Veggie buddha bowl", MealType.LUNCH),
                planItem(veggieId, "Veggie omelette", MealType.DINNER),
                planItem(veggieId, "Fruit smoothie", MealType.SNACK),
            )
        )
    }

    /** Inserts sample shopping items for the demo user to pre-populate the Shopping screen. */
    private suspend fun seedShoppingItems() {
        // Look up the demo user's ID; abort if the user somehow wasn't created.
        val demoUserId = database.userDao().getByEmail("demo@healthy.pl")?.id
            ?: return

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

        database.shoppingDao().insertAll(
            items.map { (name, qty) ->
                ShoppingItemEntity(userId = demoUserId, name = name, quantity = qty)
            }
        )
    }

    /** Factory shortcut to create a MealTemplateEntity without repeating named params everywhere. */
    private fun meal(
        name: String, type: MealType,
        kcal: Int, protein: Int, fat: Int, carbs: Int
    ) = MealTemplateEntity(
        name = name, type = type,
        kcal = kcal, proteinG = protein, fatG = fat, carbsG = carbs
    )

    /** Factory shortcut to create a PlanTemplateItemEntity with less boilerplate. */
    private fun planItem(
        planId: Long, mealName: String, mealType: MealType
    ) = PlanTemplateItemEntity(
        planTemplateId = planId,
        mealTemplateName = mealName,
        mealType = mealType
    )
}
