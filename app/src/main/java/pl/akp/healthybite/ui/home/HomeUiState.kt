package pl.akp.healthybite.ui.home

/** UI state for the Home tab – aggregated nutrition totals for today. */
data class HomeUiState(
    /** Whether entries are still being loaded; drives the LinearProgressIndicator. */
    val isLoading: Boolean = true,
    /** Today's date as ISO string (e.g. "2026-03-07"); shown below the "Today" header. */
    val date: String = "",
    /** Number of meal entries logged today; displayed in the EntriesCountCard. */
    val entriesCount: Int = 0,
    /** Sum of kcal across all today's entries; displayed in CaloriesSummaryCard. */
    val totalCalories: Int = 0,
    /** Sum of protein (grams) across all entries; displayed in MacrosSummaryCard "Protein" column. */
    val totalProteinG: Int = 0,
    /** Sum of fat (grams) across all entries; displayed in MacrosSummaryCard "Fat" column. */
    val totalFatG: Int = 0,
    /** Sum of carbs (grams) across all entries; displayed in MacrosSummaryCard "Carbs" column. */
    val totalCarbsG: Int = 0,
    /** Non-null when an error occurs (e.g. DB failure); rendered as red text below the cards. */
    val errorMessage: String? = null
)
