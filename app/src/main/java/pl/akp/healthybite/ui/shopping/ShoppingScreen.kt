package pl.akp.healthybite.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.akp.healthybite.data.db.entity.ShoppingItemEntity

/**
 * Shopping tab – a checklist-style grocery list.
 *
 * Features an inline add-item form at the top, checkboxes to mark items
 * as purchased (which dims and strikes through the text), and swipe-to-delete
 * with a confirmation dialog.
 */
@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel) {
    val state by viewModel.uiState.collectAsState()

    /*
     * Delete-confirmation dialog: shown whenever pendingDeleteId is non-null.
     * Looks up the item name for display; falls back to "this item" if the
     * item was removed from the list before the dialog renders.
     */
    if (state.pendingDeleteId != null) {
        val item = state.items.firstOrNull { it.id == state.pendingDeleteId }
        DeleteItemDialog(
            itemName = item?.name ?: "this item",
            onConfirm = viewModel::onDeleteConfirmed,
            onDismiss = viewModel::onDeleteDismissed
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Thin indeterminate progress bar at the very top while items are loading
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        /*
         * LazyColumn layout order:
         *   1. "Shopping list" header
         *   2. Inline add-item form (always visible)
         *   3. Shopping item rows (if any)
         *   4. Empty-state placeholder (if no items after loading)
         *   5. Error text (if any operation failed)
         *   6. Bottom spacer for navigation-bar clearance
         */
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Shopping list",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item(key = "add_form") {
                AddItemCard(
                    nameInput = state.nameInput,
                    quantityInput = state.quantityInput,
                    nameError = state.nameError,
                    canAdd = state.canAdd,
                    onNameChanged = viewModel::onNameChanged,
                    onQuantityChanged = viewModel::onQuantityChanged,
                    onAddClicked = viewModel::onAddClicked
                )
            }

            if (!state.isLoading && state.items.isNotEmpty()) {
                items(state.items, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggle = { checked -> viewModel.onToggleChecked(item.id, checked) },
                        onDelete = { viewModel.onDeleteRequested(item.id) }
                    )
                }
            }

            // Shown only after loading completes with zero items
            if (!state.isLoading && state.items.isEmpty()) {
                item(key = "empty") {
                    EmptyShoppingContent()
                }
            }

            state.errorMessage?.let { msg ->
                item(key = "error") {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Inline form card for adding a new shopping item.
 *
 * Contains a name field (required) and a quantity field (optional) side by side,
 * plus a full-width "Add" button. The button is disabled until [canAdd] is true
 * (name non-blank and no validation error).
 */
@Composable
private fun AddItemCard(
    nameInput: String,
    quantityInput: String,
    nameError: String?,
    canAdd: Boolean,
    onNameChanged: (String) -> Unit,
    onQuantityChanged: (String) -> Unit,
    onAddClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Name field takes remaining width; shows red border + error text when invalid
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameChanged,
                    label = { Text("Item") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                // Fixed-width quantity field (e.g. "2 kg", "1 pack")
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = onQuantityChanged,
                    label = { Text("Qty") },
                    singleLine = true,
                    modifier = Modifier.width(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Tapping "Add" calls ShoppingViewModel.onAddClicked → inserts into DB
            FilledTonalButton(
                onClick = onAddClicked,
                enabled = canAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add")
            }
        }
    }
}

/**
 * A single shopping-list row: checkbox on the left, item text in the middle,
 * delete icon on the right.
 *
 * When [item.isChecked] is true the card dims, the name gets a strikethrough,
 * and the font weight drops — giving clear visual feedback that the item is "done".
 * Tapping the checkbox calls [onToggle] → [ShoppingViewModel.onToggleChecked].
 * Tapping the trash icon calls [onDelete] → begins the two-step delete flow.
 */
@Composable
private fun ShoppingItemRow(
    item: ShoppingItemEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // Reduce card opacity when the item is checked to visually de-emphasise it
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox toggles the isChecked state in the database
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onToggle
            )
            Column(modifier = Modifier.weight(1f)) {
                // Name text: strikethrough + dimmed when checked
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Quantity sub-label shown only if the user provided one
                if (item.quantity.isNotBlank()) {
                    Text(
                        text = item.quantity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (item.isChecked) 0.5f else 0.8f
                        )
                    )
                }
            }
            // Delete icon — starts the two-step delete confirmation flow
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Placeholder card shown when the shopping list has no items.
 * Displays a cart icon and a hint prompting the user to add their first item.
 */
@Composable
private fun EmptyShoppingContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No items yet",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add your first item above",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Confirmation dialog shown before permanently removing a shopping item.
 * Displays the item name so the user knows exactly what will be deleted.
 * "Remove" triggers [ShoppingViewModel.onDeleteConfirmed]; "Cancel" triggers
 * [ShoppingViewModel.onDeleteDismissed].
 */
@Composable
private fun DeleteItemDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove this item?") },
        text = { Text("\"$itemName\" will be removed from your shopping list.") },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Remove") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
