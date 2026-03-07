package pl.akp.healthybite.ui.shopping

import pl.akp.healthybite.data.db.entity.ShoppingItemEntity

/**
 * UI state for the Shopping tab.
 *
 * [pendingDeleteId] is set when the user taps delete on an item;
 * a confirmation dialog is shown before the actual deletion.
 */
data class ShoppingUiState(
    /** True while the initial shopping item list is being loaded from Room. */
    val isLoading: Boolean = true,
    /** The current user's shopping items, observed reactively from the database. */
    val items: List<ShoppingItemEntity> = emptyList(),
    /** Two-way bound text for the "Item name" field in the add-item form. */
    val nameInput: String = "",
    /** Two-way bound text for the optional "Quantity" field in the add-item form. */
    val quantityInput: String = "",
    /** Inline validation error shown beneath the name field (e.g. "Name required"). */
    val nameError: String? = null,
    /** General error message displayed at the bottom of the list (e.g. network/DB failures). */
    val errorMessage: String? = null,
    /**
     * When non-null, the delete-confirmation dialog is shown for the item with this ID.
     * Set by [ShoppingViewModel.onDeleteRequested], cleared by confirm or dismiss.
     */
    val pendingDeleteId: Long? = null
) {
    /**
     * The "Add" button is enabled only when the name is non-blank and has no
     * validation error. Quantity is optional, so it doesn't affect this.
     */
    val canAdd: Boolean
        get() = nameInput.isNotBlank() && nameError == null
}
