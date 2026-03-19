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
import pl.akp.healthybite.data.db.dao.ShoppingDao
import pl.akp.healthybite.data.db.entity.ShoppingItemEntity

/**
 * ViewModel for the Shopping tab.
 *
 * Observes the current user's shopping items via [ShoppingDao] and
 * provides add / toggle-check / delete operations. Deletion goes through
 * a two-step flow (request → confirm) to prevent accidental removals.
 */
class ShoppingViewModel(
    private val sessionDataStore: SessionDataStore,
    private val shoppingDao: ShoppingDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingUiState())
    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    /** Begin observing shopping items as soon as the ViewModel is created. */
    init {
        observeItems()
    }

    /**
     * Sets up a reactive chain:
     *   sessionDataStore.currentUserId (Flow<Long?>)
     *     → flatMapLatest: when userId changes, switch to shoppingDao.observeItemsByUser(userId)
     *     → collectLatest: push every new item list into _uiState
     *
     * If no user is logged in (userId == null), emits an empty list so the UI
     * stays consistent rather than showing stale data from a previous user.
     */
    @Suppress("OPT_IN_USAGE")
    private fun observeItems() {
        viewModelScope.launch {
            sessionDataStore.currentUserId
                .flatMapLatest { userId ->
                    if (userId != null) shoppingDao.observeItemsByUser(userId)
                    else flowOf(emptyList())
                }
                .collectLatest { items ->
                    _uiState.update { it.copy(isLoading = false, items = items) }
                }
        }
    }

    /** Called on every keystroke in the "Item name" field; clears any prior validation error. */
    fun onNameChanged(value: String) {
        _uiState.update { it.copy(nameInput = value, nameError = null) }
    }

    /** Called on every keystroke in the "Quantity" field. No validation — quantity is optional. */
    fun onQuantityChanged(value: String) {
        _uiState.update { it.copy(quantityInput = value) }
    }

    /**
     * Triggered when the user taps the "Add" button.
     *
     * 1. Validates the name (must be non-blank); shows inline error if empty.
     * 2. Gets the current userId from the session; shows error if logged out.
     * 3. Inserts a new [ShoppingItemEntity] into Room via [ShoppingDao].
     * 4. On success, clears both input fields so the form is ready for the next item.
     *    The new item appears automatically because [observeItems] is collecting the Flow.
     */
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
                shoppingDao.insert(
                    ShoppingItemEntity(
                        userId = userId,
                        name = state.nameInput.trim(),
                        quantity = state.quantityInput.trim()
                    )
                )
                // Clear the form on success so the user can add the next item immediately
                _uiState.update { it.copy(nameInput = "", quantityInput = "", nameError = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Add failed: ${e.message}") }
            }
        }
    }

    /**
     * Toggles the checked/unchecked state of a shopping item in the database.
     * Called when the user taps the checkbox. The UI updates automatically via
     * the reactive Flow in [observeItems].
     */
    fun onToggleChecked(itemId: Long, checked: Boolean) {
        viewModelScope.launch {
            try {
                shoppingDao.updateChecked(itemId, checked)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Update failed: ${e.message}") }
            }
        }
    }

    /**
     * Step 1 of the two-step delete flow: records the item ID and triggers the
     * confirmation dialog. No data is deleted yet.
     */
    fun onDeleteRequested(itemId: Long) {
        _uiState.update { it.copy(pendingDeleteId = itemId) }
    }

    /** The user cancelled the delete dialog — clear pendingDeleteId to hide it. */
    fun onDeleteDismissed() {
        _uiState.update { it.copy(pendingDeleteId = null) }
    }

    /**
     * Step 2 of the two-step delete flow: the user confirmed deletion.
     * Clears pendingDeleteId first (hides dialog), then deletes the item from
     * the database. The list auto-updates through the reactive Flow.
     */
    fun onDeleteConfirmed() {
        val id = _uiState.value.pendingDeleteId ?: return
        _uiState.update { it.copy(pendingDeleteId = null) }
        viewModelScope.launch {
            try {
                shoppingDao.deleteById(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Delete failed: ${e.message}") }
            }
        }
    }

    /**
     * Manual dependency injection factory.
     * Required because [ShoppingViewModel] has constructor parameters that
     * Android's default ViewModelProvider cannot supply on its own.
     */
    class Factory(
        private val sessionDataStore: SessionDataStore,
        private val shoppingDao: ShoppingDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ShoppingViewModel(sessionDataStore, shoppingDao) as T
    }
}
