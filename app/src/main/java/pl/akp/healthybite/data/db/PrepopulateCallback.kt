package pl.akp.healthybite.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room [RoomDatabase.Callback] that inserts initial seed data via raw SQL
 * the very first time the database file is created.
 *
 * This runs at the SQLite level (before Room DAOs are available), so it
 * uses raw `INSERT` statements. [DatabaseSeeder] provides a complementary
 * DAO-based fallback for subsequent launches if the tables are empty.
 */
class PrepopulateCallback : RoomDatabase.Callback() {

    /**
     * Called exactly once – when the SQLite database file is first created on disk.
     * Subsequent app launches skip this entirely (the file already exists).
     *
     * Room DAOs are NOT available inside this callback because the database is
     * still being initialised, so all inserts use raw SQL via SupportSQLiteDatabase.
     */
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        insertDemoUser(db)
        insertMealTemplates(db)
        insertPlanTemplates(db)
        insertPlanTemplateItems(db)
        insertShoppingItems(db)
    }

    /** Inserts one demo user so the app is immediately usable without registration. */
    private fun insertDemoUser(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO users (email, password, displayName, dailyCaloriesGoal, weightKg)
            VALUES ('demo@healthy.pl', 'zaq1@WSX', 'Demo User', 2000, 78)
            """.trimIndent()
        )
    }

    /** Inserts 16 meal templates (4 per category) that appear on the Add Meal screen. */
    private fun insertMealTemplates(db: SupportSQLiteDatabase) {
        val meals = listOf(
            // Breakfast
            mt("Oatmeal with fruits", "BREAKFAST", 420, 18, 9, 65),
            mt("Scrambled eggs with avocado toast", "BREAKFAST", 520, 28, 32, 30),
            mt("Protein pancakes", "BREAKFAST", 480, 35, 12, 55),
            mt("Greek yogurt + granola", "BREAKFAST", 390, 22, 14, 42),
            // Lunch
            mt("Chicken + rice + veggies", "LUNCH", 650, 45, 14, 80),
            mt("Salmon + quinoa + broccoli", "LUNCH", 720, 42, 30, 55),
            mt("Turkey meatballs", "LUNCH", 680, 50, 20, 60),
            mt("Veggie buddha bowl", "LUNCH", 600, 22, 18, 85),
            // Dinner
            mt("Chicken salad", "DINNER", 450, 38, 18, 25),
            mt("Wholegrain sandwiches", "DINNER", 400, 25, 12, 50),
            mt("Veggie omelette", "DINNER", 350, 30, 20, 10),
            mt("Cottage cheese with chives", "DINNER", 300, 28, 10, 20),
            // Snack
            mt("Apple + almonds", "SNACK", 220, 6, 14, 20),
            mt("Protein bar", "SNACK", 250, 20, 8, 23),
            mt("Banana + peanut butter", "SNACK", 300, 8, 16, 35),
            mt("Fruit smoothie", "SNACK", 280, 10, 5, 50),
        )
        for ((name, type, kcal, p, f, c) in meals) {
            db.execSQL(
                """INSERT INTO meal_templates (name, type, kcal, proteinG, fatG, carbsG)
                   VALUES ('$name', '$type', $kcal, $p, $f, $c)""".trimIndent()
            )
        }
    }

    /** Inserts 3 named meal plans shown on the Plans screen. */
    private fun insertPlanTemplates(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO plan_templates (name) VALUES ('Cutting (~1800 kcal)')")
        db.execSQL("INSERT INTO plan_templates (name) VALUES ('High Protein (~2200 kcal)')")
        db.execSQL("INSERT INTO plan_templates (name) VALUES ('Veggie (~1900 kcal)')")
    }

    /** Inserts 12 plan items (4 meals per plan), linking each to its parent plan by ID. */
    private fun insertPlanTemplateItems(db: SupportSQLiteDatabase) {
        val items = listOf(
            // Cutting (planId = 1)
            pi(1, "Oatmeal with fruits", "BREAKFAST"),
            pi(1, "Chicken + rice + veggies", "LUNCH"),
            pi(1, "Chicken salad", "DINNER"),
            pi(1, "Apple + almonds", "SNACK"),
            // High Protein (planId = 2)
            pi(2, "Protein pancakes", "BREAKFAST"),
            pi(2, "Turkey meatballs", "LUNCH"),
            pi(2, "Cottage cheese with chives", "DINNER"),
            pi(2, "Protein bar", "SNACK"),
            // Veggie (planId = 3)
            pi(3, "Greek yogurt + granola", "BREAKFAST"),
            pi(3, "Veggie buddha bowl", "LUNCH"),
            pi(3, "Veggie omelette", "DINNER"),
            pi(3, "Fruit smoothie", "SNACK"),
        )
        for ((planId, mealName, mealType) in items) {
            db.execSQL(
                """INSERT INTO plan_template_items (planTemplateId, mealTemplateName, mealType)
                   VALUES ($planId, '$mealName', '$mealType')""".trimIndent()
            )
        }
    }

    /** Inserts sample shopping list items for the demo user (userId = 1). */
    private fun insertShoppingItems(db: SupportSQLiteDatabase) {
        val items = listOf(
            "Oats 500 g", "Chicken breast 1 kg", "Basmati rice 1 kg",
            "Salmon 2 fillets", "Broccoli 2 pcs", "Eggs 10 pcs",
            "Greek yogurt 3 pcs", "Almonds 200 g", "Bananas 6 pcs"
        )
        for (name in items) {
            db.execSQL(
                "INSERT INTO shopping_items (userId, name, isChecked) VALUES (1, '$name', 0)"
            )
        }
    }

    /** Factory shortcut that wraps raw meal values into a MealRow for destructuring in the loop. */
    private fun mt(name: String, type: String, kcal: Int, p: Int, f: Int, c: Int) =
        MealRow(name, type, kcal, p, f, c)

    /** Factory shortcut that wraps plan-item values into a PlanItemRow for destructuring. */
    private fun pi(planId: Long, mealName: String, mealType: String) =
        PlanItemRow(planId, mealName, mealType)

    /**
     * Lightweight data class used as a destructuring container for meal template values.
     * Allows the for-loop to destructure (name, type, kcal, p, f, c) directly.
     */
    private data class MealRow(
        val name: String, val type: String,
        val kcal: Int, val p: Int, val f: Int, val c: Int
    )

    /**
     * Lightweight data class used as a destructuring container for plan item values.
     * Allows the for-loop to destructure (planId, mealName, mealType) directly.
     */
    private data class PlanItemRow(
        val planId: Long, val mealName: String, val mealType: String
    )
}
