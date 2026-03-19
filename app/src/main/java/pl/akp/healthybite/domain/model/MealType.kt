package pl.akp.healthybite.domain.model

/**
 * Categorises meals into four daily slots.
 *
 * Stored in Room via [EnumConverters] as the enum name string.
 * Used to filter templates, tag meal entries, and organise plan items.
 */
enum class MealType {
    BREAKFAST,  // Morning meal slot – typically the first entry of the day
    LUNCH,      // Midday meal slot
    DINNER,     // Evening meal slot
    SNACK       // Catch-all slot for between-meal entries
    // Each value is persisted as its name string (e.g. "BREAKFAST") in Room
    // via EnumConverters and restored with MealType.valueOf() on read.
}

