package pl.akp.healthybite.ui.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.db.dao.WaterDao
import pl.akp.healthybite.data.db.entity.WaterEntryEntity
import java.time.LocalDate

/**
 * ViewModel for the Water tab.
 *
 * Observes the summed water intake for today (via [WaterDao.observeTotalForDate])
 * and inserts new water entries when the user taps one of the quick-add buttons.
 */
class WaterViewModel(
    private val sessionDataStore: SessionDataStore,
    private val waterDao: WaterDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterUiState())
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    /** Kick off observation as soon as the ViewModel is created. */
    init {
        observeTodayTotal()
    }

    /**
     * Sets up a reactive chain that keeps [WaterUiState.totalMl] in sync:
     *
     *   sessionDataStore.currentUserId (Flow<Long?>)
     *     → flatMapLatest: when userId changes, switch to
     *       waterDao.observeTotalForDate(userId, today)  (Flow<Int?>)
     *     → map: convert null (no entries yet) to 0
     *     → collectLatest: push every new total into _uiState
     *
     * Because the DAO returns a Room Flow, adding a new entry in [onAdd]
     * automatically triggers a re-emission here — no manual refresh needed.
     */
    @Suppress("OPT_IN_USAGE")
    private fun observeTodayTotal() {
        val today = LocalDate.now().toString()
        _uiState.update { it.copy(date = today, isLoading = true) }

        viewModelScope.launch {
            sessionDataStore.currentUserId
                .flatMapLatest { userId ->
                    if (userId != null) {
                        waterDao.observeTotalForDate(userId, today).map { it ?: 0 }
                    } else {
                        flowOf(0)
                    }
                }
                .collectLatest { total ->
                    _uiState.update { it.copy(isLoading = false, totalMl = total) }
                }
        }
    }

    /**
     * Called when the user taps one of the quick-add buttons (+250 / +500 / +750 ml).
     *
     * 1. Gets the current userId from the session.
     * 2. Creates a [WaterEntryEntity] stamped with today's date and the current timestamp.
     * 3. Inserts it into Room via [WaterDao].
     *
     * Because [observeTodayTotal] collects a Room Flow, the UI total updates
     * automatically after the insert — no manual state mutation needed here.
     */
    fun onAdd(amountMl: Int) {
        viewModelScope.launch {
            val userId = sessionDataStore.currentUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "Not logged in") }
                return@launch
            }
            try {
                waterDao.insert(
                    WaterEntryEntity(
                        userId = userId,
                        date = LocalDate.now().toString(),
                        amountMl = amountMl,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to add: ${e.message}") }
            }
        }
    }

    /**
     * Manual dependency injection factory.
     * Required because [WaterViewModel] has constructor parameters that
     * Android's default ViewModelProvider cannot supply on its own.
     */
    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val waterDao: WaterDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WaterViewModel(sessionDataStore, waterDao) as T
    }
}
