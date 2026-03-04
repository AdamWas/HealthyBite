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

class WaterViewModel(
    private val sessionDataStore: SessionDataStore,
    private val waterDao: WaterDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterUiState())
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    init {
        observeTodayTotal()
    }

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

    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val waterDao: WaterDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WaterViewModel(sessionDataStore, waterDao) as T
    }
}
