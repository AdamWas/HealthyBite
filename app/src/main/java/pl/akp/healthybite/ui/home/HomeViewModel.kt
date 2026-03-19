package pl.akp.healthybite.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.db.dao.MealEntryDao
import pl.akp.healthybite.data.db.entity.MealEntryEntity
import java.time.LocalDate

/**
 * ViewModel for the Home tab.
 *
 * Reactively observes today's meal entries (scoped to the logged-in user via
 * [SessionDataStore.currentUserId]) and computes aggregate nutrition totals
 * (calories, protein, fat, carbs) that are displayed on the dashboard cards.
 */
class HomeViewModel(
    private val sessionDataStore: SessionDataStore,
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    /** Mutable backing field for the UI state; only this ViewModel writes to it. */
    private val _uiState = MutableStateFlow(HomeUiState())
    /** Public read-only state consumed by HomeScreen via collectAsState(). */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Begin observing as soon as the ViewModel is created (during navigation).
        observeTodayEntries()
    }

    /*
     * Sets up a reactive pipeline that automatically refreshes the UI
     * whenever Room's underlying data changes:
     *
     *   sessionDataStore.currentUserId   (Flow<Long?>)
     *       │  emits the logged-in user's ID (or null if signed out)
     *       ▼
     *   flatMapLatest                     (switches to a new inner Flow on each emission)
     *       │  maps userId → mealEntryDao.observeEntriesForUserAndDate(userId, today)
     *       ▼
     *   collectLatest                     (receives List<MealEntryEntity> on every DB change)
     *       │  sums kcal / protein / fat / carbs via computeTotals()
     *       ▼
     *   _uiState                          (updated HomeUiState triggers recomposition)
     *
     * @Suppress("OPT_IN_USAGE") silences the compiler warning for the
     * experimental kotlinx.coroutines flatMapLatest API.
     */
    @Suppress("OPT_IN_USAGE")
    private fun observeTodayEntries() {
        // Captured once at ViewModel creation — won't update if the user keeps
        // the app open past midnight. A process-death or navigation rebuild
        // would create a new ViewModel with the correct date.
        val today = LocalDate.now().toString()
        _uiState.value = _uiState.value.copy(date = today, isLoading = true)

        viewModelScope.launch {
            sessionDataStore.currentUserId
                .flatMapLatest { userId ->
                    if (userId != null) {
                        // Room returns a reactive Flow that re-emits whenever
                        // the meal_entries table changes for this user + date.
                        mealEntryDao.observeEntriesForUserAndDate(userId, today)
                    } else {
                        // No user logged in — emit an empty list so the UI
                        // shows the empty-meals state.
                        flowOf(emptyList())
                    }
                }
                .collectLatest { entries ->
                    val totals = computeTotals(entries)
                    _uiState.value = totals.copy(date = today, isLoading = false)
                }
        }
    }

    /**
     * Manual dependency-injection factory.
     *
     * Used because Hilt / Koin is not set up in this project. The NavGraph
     * creates the Factory with concrete DAO and DataStore instances and
     * passes it to viewModel().
     */
    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val mealEntryDao: MealEntryDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(sessionDataStore, mealEntryDao) as T
    }
}

/**
 * Sums kcal and macros across all entries – also usable from tests without a ViewModel.
 *
 * Declared as a top-level function (not a member of HomeViewModel) so unit tests
 * can call it directly without instantiating a ViewModel or its dependencies.
 */
fun computeTotals(entries: List<MealEntryEntity>): HomeUiState {
    return HomeUiState(
        entriesCount = entries.size,
        totalCalories = entries.sumOf { it.kcal },
        totalProteinG = entries.sumOf { it.proteinG },
        totalFatG = entries.sumOf { it.fatG },
        totalCarbsG = entries.sumOf { it.carbsG }
    )
}
