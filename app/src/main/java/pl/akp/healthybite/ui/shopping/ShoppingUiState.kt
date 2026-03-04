package pl.akp.healthybite.ui.shopping

import pl.akp.healthybite.data.db.entity.ShoppingItemEntity

data class ShoppingUiState(
    val isLoading: Boolean = true,
    val items: List<ShoppingItemEntity> = emptyList(),
    val nameInput: String = "",
    val quantityInput: String = "",
    val nameError: String? = null,
    val errorMessage: String? = null,
    val pendingDeleteId: Long? = null
) {
    val canAdd: Boolean
        get() = nameInput.isNotBlank() && nameError == null
}
