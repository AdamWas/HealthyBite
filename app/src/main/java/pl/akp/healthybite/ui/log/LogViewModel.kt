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

class LogViewModel(
    private val sessionDataStore: SessionDataStore,
    private val mealEntryDao: MealEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        observeTodayEntries()
    }

    @Suppress("OPT_IN_USAGE")
    private fun observeTodayEntries() {
        val today = LocalDate.now().toString()
        _uiState.update { it.copy(date = today, isLoading = true) }

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

    fun onDeleteEntry(entryId: Long) {
        viewModelScope.launch {
            try {
                mealEntryDao.deleteById(entryId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Delete failed: ${e.message}") }
            }
        }
    }

    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val mealEntryDao: MealEntryDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LogViewModel(sessionDataStore, mealEntryDao) as T
    }
}
