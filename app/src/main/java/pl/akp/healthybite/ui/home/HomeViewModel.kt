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

class HomeViewModel(
    private val sessionDataStore: SessionDataStore,
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTodayEntries()
    }

    @Suppress("OPT_IN_USAGE")
    private fun observeTodayEntries() {
        val today = LocalDate.now().toString()
        _uiState.value = _uiState.value.copy(date = today, isLoading = true)

        viewModelScope.launch {
            sessionDataStore.currentUserId
                .flatMapLatest { userId ->
                    if (userId != null) {
                        mealEntryDao.observeEntriesForUserAndDate(userId, today)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collectLatest { entries ->
                    val totals = computeTotals(entries)
                    _uiState.value = totals.copy(date = today, isLoading = false)
                }
        }
    }

    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val mealEntryDao: MealEntryDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(sessionDataStore, mealEntryDao) as T
    }
}

fun computeTotals(entries: List<MealEntryEntity>): HomeUiState {
    return HomeUiState(
        entriesCount = entries.size,
        totalCalories = entries.sumOf { it.kcal },
        totalProteinG = entries.sumOf { it.proteinG },
        totalFatG = entries.sumOf { it.fatG },
        totalCarbsG = entries.sumOf { it.carbsG }
    )
}
