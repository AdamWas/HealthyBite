package pl.akp.healthybite.ui.water

/**
 * UI state for the Water tab.
 *
 * [progress] is a derived 0..1 fraction used to animate the progress bar.
 * The default [goalMl] of 2000 ml is a standard daily hydration target.
 */
data class WaterUiState(
    /** True while the initial water total is being loaded. */
    val isLoading: Boolean = true,
    /** Today's date as an ISO string (e.g. "2026-03-07"), shown beneath the header. */
    val date: String = "",
    /** Running total of water consumed today in millilitres, summed from all entries. */
    val totalMl: Int = 0,
    /** Daily hydration target in ml. Defaults to the commonly recommended 2000 ml. */
    val goalMl: Int = 2000,
    /** Error text shown if a database insert or session lookup fails. */
    val errorMessage: String? = null
) {
    /**
     * Progress fraction (0.0–1.0) used by the animated progress bar.
     * Clamped to 1.0 so the bar never overflows even if the user exceeds the goal.
     * Returns 0 when goalMl is zero to avoid division by zero.
     */
    val progress: Float
        get() = if (goalMl > 0) (totalMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
}
