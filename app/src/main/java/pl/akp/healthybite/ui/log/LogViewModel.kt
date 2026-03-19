package pl.akp.healthybite.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.db.dao.MealEntryDao
import pl.akp.healthybite.data.db.entity.MealEntryEntity
import java.time.LocalDate

/**
 * ViewModel for the Log tab.
 *
 * Observes today's meal entries for the current user and exposes them
 * as a list with running nutrition totals. Supports entry deletion with
 * automatic UI refresh via Room's reactive Flow.
 */
class LogViewModel(
    private val sessionDataStore: SessionDataStore,
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    /** Mutable backing field for the UI state; only this ViewModel writes to it. */
    private val _uiState = MutableStateFlow(LogUiState())
    /** Public read-only state consumed by LogScreen via collectAsState(). */
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        // Begin observing as soon as the ViewModel is created (during navigation).
        observeTodayEntries()
    }

    /*
     * Sets up the same reactive pipeline used by HomeViewModel:
     *
     *   sessionDataStore.currentUserId → flatMapLatest → mealEntryDao.observeEntriesForUserAndDate
     *       → collectLatest → update _uiState
     *
     * Unlike HomeViewModel, totals are computed inline inside the collectLatest
     * lambda rather than in a separate top-level function, because LogUiState
     * also stores the raw entries list (needed to render individual EntryCards).
     *
     * @Suppress("OPT_IN_USAGE") silences the compiler warning for the
     * experimental kotlinx.coroutines flatMapLatest API.
     */
    @Suppress("OPT_IN_USAGE")
    private fun observeTodayEntries() {
        // Captured once at ViewModel creation — won't update if the user keeps
        // the app open past midnight. See HomeViewModel for the same caveat.
        val today = LocalDate.now().toString()
        _uiState.update { it.copy(date = today, isLoading = true) }

        viewModelScope.launch {
            sessionDataStore.currentUserId
                .flatMapLatest { userId ->
                    if (userId != null) {
                        // Room returns a reactive Flow that re-emits whenever
                        // the meal_entries table changes for this user + date.
                        mealEntryDao.observeEntriesForUserAndDate(userId, today)
                    } else {
                        // No user logged in — emit an empty list.
                        flowOf(emptyList())
                    }
                }
                .collectLatest { entries ->
                    // Totals are computed inline here (not via a shared
                    // function) because we also store the raw entries list
                    // in LogUiState for per-entry card rendering.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            date = today,
                            entries = entries,
                            totalCalories = entries.sumOf { e -> e.kcal },
                            totalProteinG = entries.sumOf { e -> e.proteinG },
                            totalFatG = entries.sumOf { e -> e.fatG },
                            totalCarbsG = entries.sumOf { e -> e.carbsG },
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /**
     * Deletes a meal entry by its primary key.
     *
     * After the DAO removes the row, Room's reactive Flow automatically
     * re-emits the updated list, which collectLatest picks up and pushes
     * into _uiState — so no manual refresh call is needed.
     * On failure the error is surfaced via errorMessage in the UI state.
     */
    fun onDeleteEntry(entryId: Long) {
        viewModelScope.launch {
            try {
                mealEntryDao.deleteById(entryId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Delete failed: ${e.message}") }
            }
        }
    }

    /**
     * Manual dependency-injection factory (same pattern as HomeViewModel.Factory).
     *
     * The NavGraph creates this Factory with concrete DAO and DataStore instances
     * and passes it to viewModel().
     */
    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val mealEntryDao: MealEntryDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LogViewModel(sessionDataStore, mealEntryDao) as T
    }
}
