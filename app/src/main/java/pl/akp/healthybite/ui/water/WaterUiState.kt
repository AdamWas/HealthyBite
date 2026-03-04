package pl.akp.healthybite.ui.water

data class WaterUiState(
    val isLoading: Boolean = true,
    val date: String = "",
    val totalMl: Int = 0,
    val goalMl: Int = 2000,
    val errorMessage: String? = null
) {
    val progress: Float
        get() = if (goalMl > 0) (totalMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
}
