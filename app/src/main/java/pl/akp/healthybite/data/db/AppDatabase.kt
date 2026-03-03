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
    version = 2,
    exportSchema = false
)
@TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun mealTemplateDao(): MealTemplateDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun planDao(): PlanDao
    abstract fun waterDao(): WaterDao
}

