package pl.akp.healthybite.domain.model

data class ShoppingItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val quantity: String = "",
    val isChecked: Boolean = false
)
