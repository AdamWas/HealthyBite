package pl.akp.healthybite.ui.log

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
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.akp.healthybite.data.db.entity.MealEntryEntity
import pl.akp.healthybite.domain.model.MealType

/**
 * Log tab – shows today's meal entries in a scrollable list.
 *
 * Each entry card displays the meal name, type badge, calories and macros.
 * Entries can be deleted via a confirmation dialog. A summary card at the
 * top aggregates totals. Empty state prompts the user to add a meal.
 */
@Composable
fun LogScreen(
    viewModel: LogViewModel,
    onNavigateToAddMeal: () -> Unit
) {
    // Converts the ViewModel's StateFlow into Compose State so that
    // this composable recomposes whenever the data changes.
    val state by viewModel.uiState.collectAsState()

    // Two-step delete flow:
    // 1. User taps the delete icon on an EntryCard → entryToDelete is set.
    // 2. A DeleteConfirmDialog appears showing the entry name.
    // 3. On confirm → viewModel.onDeleteEntry() is called, and the state is cleared.
    // 4. On dismiss → the state is simply cleared without any deletion.
    var entryToDelete by remember { mutableStateOf<MealEntryEntity?>(null) }

    entryToDelete?.let { entry ->
        DeleteConfirmDialog(
            entryName = entry.name,
            onConfirm = {
                viewModel.onDeleteEntry(entry.id)
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Indeterminate progress bar shown while entries are loading.
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // LazyColumn is used instead of Column + verticalScroll because it
        // lazily composes only the visible items — important when the user
        // has many logged meals. Stable keys (entry.id) let Compose diff
        // items efficiently on insertions / deletions.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Header: "Today's log" with the date underneath ──
            item(key = "header") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Today's log",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (state.date.isNotEmpty()) {
                    Text(
                        text = state.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!state.isLoading && state.entries.isNotEmpty()) {
                // ── Summary card: aggregated kcal + macros at top of list ──
                item(key = "summary") {
                    SummaryCard(state)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ── Individual entry cards ──
                // Each card shows meal name, type badge, kcal, macros,
                // and a delete icon. Tapping delete sets entryToDelete
                // which triggers the confirmation dialog above.
                items(state.entries, key = { it.id }) { entry ->
                    EntryCard(
                        entry = entry,
                        onDeleteClick = { entryToDelete = entry }
                    )
                }
            }

            // ── Empty state: shown when no meals have been logged ──
            if (!state.isLoading && state.entries.isEmpty()) {
                item(key = "empty") {
                    EmptyLogContent(onAddMealClick = onNavigateToAddMeal)
                }
            }

            // ── Error message (e.g. failed delete) rendered in red ──
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
 * Aggregated summary card pinned at the top of the log list.
 *
 * First row: total kcal on the left, meal count on the right.
 * Second row: three MacroChip columns (Protein / Fat / Carbs) evenly spaced.
 * Uses primaryContainer to visually distinguish it from individual entry cards.
 */
@Composable
private fun SummaryCard(state: LogUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.totalCalories} kcal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${state.entries.size} meal${if (state.entries.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroChip(label = "Protein", valueG = state.totalProteinG)
                MacroChip(label = "Fat", valueG = state.totalFatG)
                MacroChip(label = "Carbs", valueG = state.totalCarbsG)
            }
        }
    }
}

/**
 * A single macro column inside the SummaryCard.
 *
 * Displays the gram value (e.g. "45g") in bold on top and the label
 * (e.g. "Protein") underneath. Styled with the primaryContainer on-color.
 */
@Composable
private fun MacroChip(label: String, valueG: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${valueG}g",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * Card representing a single logged meal entry.
 *
 * Layout:
 *   Row 1: meal name (ellipsized if long) + MealTypeBadge (e.g. "Lunch")
 *   Row 2: kcal in primary color + compact macro string "P 12g · F 8g · C 45g"
 *   Right side: a delete IconButton (red trash icon) that triggers the
 *               two-step delete confirmation flow via onDeleteClick.
 */
@Composable
private fun EntryCard(
    entry: MealEntryEntity,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    MealTypeBadge(type = entry.mealType)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${entry.kcal} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "P ${entry.proteinG}g · F ${entry.fatG}g · C ${entry.carbsG}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete entry",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Small colored chip displaying the meal type (Breakfast / Lunch / Dinner / Snack).
 *
 * Uses the tertiaryContainer color to contrast with the entry card's
 * surfaceVariant background, making the badge easy to spot at a glance.
 */
@Composable
private fun MealTypeBadge(type: MealType) {
    val label = when (type) {
        MealType.BREAKFAST -> "Breakfast"
        MealType.LUNCH -> "Lunch"
        MealType.DINNER -> "Dinner"
        MealType.SNACK -> "Snack"
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/**
 * Empty-state content shown when there are no meals logged for today.
 *
 * Displays a book icon, an encouraging message, and an "Add meal" button
 * that navigates to the AddMealScreen via the onAddMealClick callback.
 */
@Composable
private fun EmptyLogContent(onAddMealClick: () -> Unit) {
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
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No meals logged today",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start tracking your nutrition",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(onClick = onAddMealClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add meal")
                }
            }
        }
    }
}

/**
 * Confirmation dialog shown before deleting a meal entry.
 *
 * Displays the entry name in quotes and warns the user that the action
 * cannot be undone. "Delete" triggers onConfirm (which calls
 * viewModel.onDeleteEntry); "Cancel" dismisses without side effects.
 */
@Composable
private fun DeleteConfirmDialog(
    entryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this entry?") },
        text = {
            Text("\"$entryName\" will be removed from today's log. This cannot be undone.")
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
