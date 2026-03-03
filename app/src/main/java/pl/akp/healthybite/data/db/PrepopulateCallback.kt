package pl.akp.healthybite.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class PrepopulateCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        insertDemoUser(db)
        insertMealTemplates(db)
        insertPlanTemplates(db)
        insertPlanTemplateItems(db)
        insertShoppingItems(db)
    }

    private fun insertDemoUser(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO users (email, password, displayName, dailyCaloriesGoal, weightKg)
            VALUES ('demo@healthy.pl', 'zaq1@WSX', 'Demo User', 2000, 78)
            """.trimIndent()
        )
    }

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

    private fun insertPlanTemplates(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO plan_templates (name) VALUES ('Cutting (~1800 kcal)')")
        db.execSQL("INSERT INTO plan_templates (name) VALUES ('High Protein (~2200 kcal)')")
        db.execSQL("INSERT INTO plan_templates (name) VALUES ('Veggie (~1900 kcal)')")
    }

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

    private fun mt(name: String, type: String, kcal: Int, p: Int, f: Int, c: Int) =
        MealRow(name, type, kcal, p, f, c)

    private fun pi(planId: Long, mealName: String, mealType: String) =
        PlanItemRow(planId, mealName, mealType)

    private data class MealRow(
        val name: String, val type: String,
        val kcal: Int, val p: Int, val f: Int, val c: Int
    )

    private data class PlanItemRow(
        val planId: Long, val mealName: String, val mealType: String
    )
}
