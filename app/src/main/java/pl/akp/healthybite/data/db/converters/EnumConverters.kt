package pl.akp.healthybite.data.db.converters

import androidx.room.TypeConverter
import pl.akp.healthybite.domain.model.MealType

/**
 * Room [TypeConverter]s for persisting enum types as strings.
 *
 * [MealType] values are stored by their enum name (e.g. "BREAKFAST")
 * and converted back with [MealType.valueOf].
 */
class EnumConverters {

    /**
     * Converts a MealType enum to its String name for storage in SQLite.
     * Room calls this automatically when writing any column typed as MealType.
     * Example: MealType.BREAKFAST -> "BREAKFAST"
     */
    @TypeConverter
    fun fromMealType(type: MealType?): String? {
        return type?.name
    }

    /**
     * Converts a stored String back to the MealType enum when reading from SQLite.
     * Room calls this automatically when populating any entity field typed as MealType.
     * Example: "BREAKFAST" -> MealType.BREAKFAST
     */
    @TypeConverter
    fun toMealType(value: String?): MealType? {
        return value?.let { MealType.valueOf(it) }
    }
}

