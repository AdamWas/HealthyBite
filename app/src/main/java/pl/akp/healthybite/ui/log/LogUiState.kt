package pl.akp.healthybite.ui.log

import pl.akp.healthybite.data.db.entity.MealEntryEntity

/** UI state for the Log tab – the full list of today's entries plus running totals. */
data class LogUiState(
    /** Whether entries are still being fetched from Room; drives the LinearProgressIndicator. */
    val isLoading: Boolean = true,
    /** Today's date as ISO string (e.g. "2026-03-07"); shown below the "Today's log" header. */
    val date: String = "",
    /** Full list of today's meal entries; each one rendered as an EntryCard in the LazyColumn. */
    val entries: List<MealEntryEntity> = emptyList(),
    /** Sum of kcal across all entries; displayed in the SummaryCard at the top of the list. */
    val totalCalories: Int = 0,
    /** Sum of protein (grams); displayed in the SummaryCard "Protein" MacroChip. */
    val totalProteinG: Int = 0,
    /** Sum of fat (grams); displayed in the SummaryCard "Fat" MacroChip. */
    val totalFatG: Int = 0,
    /** Sum of carbs (grams); displayed in the SummaryCard "Carbs" MacroChip. */
    val totalCarbsG: Int = 0,
    /** Non-null when an operation fails (e.g. delete); rendered as red text in the list. */
    val errorMessage: String? = null
)
