package pl.akp.healthybite.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.akp.healthybite.data.db.converters.EnumConverters
import pl.akp.healthybite.data.db.dao.MealEntryDao
import pl.akp.healthybite.data.db.dao.MealTemplateDao
import pl.akp.healthybite.data.db.dao.PlanDao
import pl.akp.healthybite.data.db.dao.ShoppingDao
import pl.akp.healthybite.data.db.dao.UserDao
import pl.akp.healthybite.data.db.dao.WaterDao
import pl.akp.healthybite.data.db.entity.MealEntryEntity
import pl.akp.healthybite.data.db.entity.MealTemplateEntity
import pl.akp.healthybite.data.db.entity.PlanTemplateEntity
import pl.akp.healthybite.data.db.entity.PlanTemplateItemEntity
import pl.akp.healthybite.data.db.entity.ShoppingItemEntity
import pl.akp.healthybite.data.db.entity.UserEntity
import pl.akp.healthybite.data.db.entity.WaterEntryEntity

/**
 * Room database definition for HealthyBite.
 *
 * Registers all seven entities and exposes six DAO accessors.
 * [EnumConverters] handles [MealType] ↔ String serialisation.
 *
 * Schema migrations are destructive (dev-phase convenience);
 * bump [version] whenever entities change.
 */
@Database(
    entities = [
        UserEntity::class,
        MealTemplateEntity::class,
        MealEntryEntity::class,
        ShoppingItemEntity::class,
        PlanTemplateEntity::class,
        PlanTemplateItemEntity::class,
        WaterEntryEntity::class
    ],
    // Bump this version number whenever an entity's schema changes.
    // Because we use fallbackToDestructiveMigration (dev phase), Room will
    // drop and recreate all tables on version mismatch instead of migrating.
    version = 7,
    // exportSchema = false disables the JSON schema export that Room can generate
    // for migration verification. Not needed during active development.
    exportSchema = false
)
// Registers EnumConverters so Room automatically converts MealType enum <-> String
// for every column of type MealType across all entities.
@TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {

    /** Used by AuthRepositoryImpl for login/register and by ProfileViewModel for user data. */
    abstract fun userDao(): UserDao

    /** Used by AddMealViewModel to list available templates, and by DatabaseSeeder for count check. */
    abstract fun mealTemplateDao(): MealTemplateDao

    /** Used by LogViewModel / HomeViewModel to read/write the user's daily food log. */
    abstract fun mealEntryDao(): MealEntryDao

    /** Used by ShoppingViewModel to manage the user's shopping list. */
    abstract fun shoppingDao(): ShoppingDao

    /** Used by PlansViewModel to display meal plans and apply them to today's log. */
    abstract fun planDao(): PlanDao

    /** Used by WaterViewModel / HomeViewModel to track daily water intake. */
    abstract fun waterDao(): WaterDao
}

