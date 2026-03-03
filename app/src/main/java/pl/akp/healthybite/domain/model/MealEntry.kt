package pl.akp.healthybite.domain.model

import java.time.LocalDateTime

data class MealEntry(
    val id: Long,
    val templateId: Long,
    val dateTime: LocalDateTime,
    val notes: String?
)

