package pl.akp.healthybite.data.db.converters

import androidx.room.TypeConverter
import pl.akp.healthybite.domain.model.MealType

class EnumConverters {

    @TypeConverter
    fun fromMealType(type: MealType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toMealType(value: String?): MealType? {
        return value?.let { MealType.valueOf(it) }
    }
}

