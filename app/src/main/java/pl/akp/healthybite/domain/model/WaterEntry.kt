package pl.akp.healthybite.domain.model

data class WaterEntry(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val amountMl: Int = 0,
    val timestamp: Long = 0
)
