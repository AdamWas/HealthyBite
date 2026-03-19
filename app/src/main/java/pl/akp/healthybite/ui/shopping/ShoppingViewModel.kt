package pl.akp.healthybite.ui.shopping

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.domain.model.ShoppingItem
import pl.akp.healthybite.domain.repository.ShoppingRepository

class ShoppingViewModel(
    private val sessionDataStore: SessionDataStore,
    private val shoppingRepository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingUiState())
    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    init {
        observeItems()
    }

    @Suppress("OPT_IN_USAGE")
    private fun observeItems() {
        viewModelScope.launch {
            sessionDataStore.currentUserId
                .flatMapLatest { userId ->
                    if (userId != null) shoppingRepository.observeItemsByUser(userId)
                    else flowOf(emptyList())
                }
                .collectLatest { items ->
                    _uiState.update { it.copy(isLoading = false, items = items) }
                }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(nameInput = value, nameError = null) }
    }

    fun onQuantityChanged(value: String) {
        _uiState.update { it.copy(quantityInput = value) }
    }

    fun onAddClicked() {
        val state = _uiState.value
        if (state.nameInput.isBlank()) {
            _uiState.update { it.copy(nameError = "Name required") }
            return
        }

        viewModelScope.launch {
            val userId = sessionDataStore.currentUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(errorMessage = "Not logged in") }
                return@launch
            }
            try {
                shoppingRepository.insertItem(
                    ShoppingItem(
                        userId = userId,
                        name = state.nameInput.trim(),
                        quantity = state.quantityInput.trim()
                    )
                )
                _uiState.update { it.copy(nameInput = "", quantityInput = "", nameError = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Add failed: ${e.message}") }
            }
        }
    }

    fun onToggleChecked(itemId: String, checked: Boolean) {
        viewModelScope.launch {
            try {
                shoppingRepository.updateChecked(itemId, checked)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Update failed: ${e.message}") }
            }
        }
    }

    fun onDeleteRequested(itemId: String) {
        _uiState.update { it.copy(pendingDeleteId = itemId) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(pendingDeleteId = null) }
    }

    fun onDeleteConfirmed() {
        val id = _uiState.value.pendingDeleteId ?: return
        _uiState.update { it.copy(pendingDeleteId = null) }
        viewModelScope.launch {
            try {
                shoppingRepository.deleteItem(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Delete failed: ${e.message}") }
            }
        }
    }

    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val shoppingRepository: ShoppingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ShoppingViewModel(sessionDataStore, shoppingRepository) as T
    }
}
