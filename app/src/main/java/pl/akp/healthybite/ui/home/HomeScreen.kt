package pl.akp.healthybite.ui.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FoodBank
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Home tab – displays today's nutrition dashboard.
 *
 * Shows a calories summary card, macronutrient breakdown, and meal count.
 * When no meals have been logged, an empty-state prompt is shown instead.
 */
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    // Converts the ViewModel's StateFlow into Compose State so that
    // this composable automatically recomposes when the data changes.
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Indeterminate progress bar shown at the very top while entries
        // are being loaded from the database on first launch.
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // "Today" header with the current date displayed underneath.
            Text(
                text = "Today",
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

            Spacer(modifier = Modifier.height(20.dp))

            if (!state.isLoading && state.entriesCount == 0) {
                // Empty state — no meals logged yet. Shows a prompt that
                // tells the user to tap the FAB (+) to add their first meal.
                EmptyMealsCard()
            } else if (!state.isLoading) {
                // Non-empty state — show the three dashboard cards in order:
                // 1) CaloriesSummaryCard  – total kcal with a fire icon
                // 2) MacrosSummaryCard    – protein / fat / carbs breakdown
                // 3) EntriesCountCard     – "X meals logged"
                CaloriesSummaryCard(state)
                Spacer(modifier = Modifier.height(12.dp))
                MacrosSummaryCard(state)
                Spacer(modifier = Modifier.height(12.dp))
                EntriesCountCard(state.entriesCount)
            }

            // Render an error message (e.g. DB read failure) in red below
            // the cards, if one exists.
            state.errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Prominent card showing total kilocalories consumed today.
 *
 * Uses the primary container color to stand out as the most important metric.
 * A fire icon (LocalFireDepartment) sits on the left; the kcal number and
 * "calories today" label sit on the right.
 */
@Composable
private fun CaloriesSummaryCard(state: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "${state.totalCalories}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "calories today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Card showing the macronutrient breakdown: Protein, Fat, and Carbs.
 *
 * Each macro is rendered as a MacroItem column (value in grams + label),
 * evenly spaced in a Row.
 */
@Composable
private fun MacrosSummaryCard(state: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Macronutrients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem(label = "Protein", valueG = state.totalProteinG)
                MacroItem(label = "Fat", valueG = state.totalFatG)
                MacroItem(label = "Carbs", valueG = state.totalCarbsG)
            }
        }
    }
}

/**
 * A single macro column inside MacrosSummaryCard.
 *
 * Displays the gram value (e.g. "45g") in a bold primary color on top
 * and the label (e.g. "Protein") in a muted color underneath.
 */
@Composable
private fun MacroItem(label: String, valueG: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${valueG}g",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Card showing the number of meals logged today (e.g. "3 meals logged").
 *
 * Includes a restaurant icon and handles singular/plural grammar
 * ("1 meal" vs "2 meals").
 */
@Composable
private fun EntriesCountCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$count meal${if (count != 1) "s" else ""} logged",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Empty-state card shown when no meals have been logged for today.
 *
 * Displays a food icon, a "No meals logged" message, and a hint telling the
 * user to tap the floating action button (+) in the MainScaffold to add
 * their first meal.
 */
@Composable
private fun EmptyMealsCard() {
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
                    imageVector = Icons.Outlined.FoodBank,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No meals logged for today",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Use + to add your first meal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
