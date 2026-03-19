package pl.akp.healthybite.ui.shopping

import pl.akp.healthybite.domain.model.ShoppingItem

data class ShoppingUiState(
    val isLoading: Boolean = true,
    val items: List<ShoppingItem> = emptyList(),
    val nameInput: String = "",
    val quantityInput: String = "",
    val nameError: String? = null,
    val errorMessage: String? = null,
    val pendingDeleteId: String? = null
) {
    val canAdd: Boolean
        get() = nameInput.isNotBlank() && nameError == null
}
