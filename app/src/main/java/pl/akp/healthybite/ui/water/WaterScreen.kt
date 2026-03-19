package pl.akp.healthybite.ui.water

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Water tab – tracks daily water intake with quick-add buttons.
 *
 * A hero card shows the current total with an animated progress bar
 * toward the 2000 ml goal. Three buttons (+250 / +500 / +750 ml) allow
 * quick logging without typing.
 */
@Composable
fun WaterScreen(viewModel: WaterViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Indeterminate progress bar while the initial total is loading
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Water",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            // Show today's date beneath the title so the user knows which day they're logging
            if (state.date.isNotEmpty()) {
                Text(
                    text = state.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero card showing current total, goal, animated progress bar, and percentage
            TotalCard(state)

            Spacer(modifier = Modifier.height(20.dp))

            // Row of three quick-add buttons (+250, +500, +750 ml)
            AddWaterButtons(onAdd = viewModel::onAdd)

            // Hint text when the user hasn't logged any water yet today
            if (!state.isLoading && state.totalMl == 0) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Start hydrating — tap a button above",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
 * Hero card displaying the user's current hydration status:
 *   - Water-drop icon
 *   - Large "X ml" total
 *   - "of Y ml goal" sub-label
 *   - Animated progress bar (fills as total approaches goal)
 *   - Percentage label
 *
 * Uses [animateFloatAsState] so the progress bar glides smoothly when
 * [WaterUiState.totalMl] changes, rather than jumping instantly.
 */
@Composable
private fun TotalCard(state: WaterUiState) {
    // Smoothly animate the bar whenever the underlying progress fraction changes
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 400),
        label = "water_progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Current total in large bold text
            Text(
                text = "${state.totalMl} ml",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            // Goal label sits directly under the total
            Text(
                text = "of ${state.goalMl} ml goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Rounded progress bar driven by the animated value
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Numeric percentage below the bar for at-a-glance reading
            Text(
                text = "${(state.progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Row of three equally-weighted quick-add buttons (+250 / +500 / +750 ml).
 * Each button calls [onAdd] with its specific amount, which ultimately
 * inserts a [WaterEntryEntity] via [WaterViewModel.onAdd].
 */
@Composable
private fun AddWaterButtons(onAdd: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WaterButton(label = "+250 ml", amountMl = 250, onAdd = onAdd, modifier = Modifier.weight(1f))
        WaterButton(label = "+500 ml", amountMl = 500, onAdd = onAdd, modifier = Modifier.weight(1f))
        WaterButton(label = "+750 ml", amountMl = 750, onAdd = onAdd, modifier = Modifier.weight(1f))
    }
}

/**
 * A single quick-add button. Tapping it calls [onAdd] with [amountMl],
 * which flows through to [WaterViewModel.onAdd] → DAO insert.
 */
@Composable
private fun WaterButton(
    label: String,
    amountMl: Int,
    onAdd: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = { onAdd(amountMl) },
        modifier = modifier.height(48.dp)
    ) {
        Text(label)
    }
}
