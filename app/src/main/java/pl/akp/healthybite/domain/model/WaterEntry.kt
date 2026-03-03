package pl.akp.healthybite.domain.model

import java.time.LocalDateTime

data class WaterEntry(
    val id: Long,
    val amountMl: Int,
    val dateTime: LocalDateTime
)

