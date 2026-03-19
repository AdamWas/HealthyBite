package pl.akp.healthybite.ui.plans

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.akp.healthybite.domain.model.MealType

/**
 * Plans tab – displays predefined meal plans that the user can apply to today.
 *
 * Each plan card shows its name, total kcal, and a breakdown of meal items
 * with type badges. The "Apply to today" button bulk-inserts the plan's
 * meals into today's log and shows a snackbar confirmation.
 */
@Composable
fun PlansScreen(viewModel: PlansViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    /*
     * Two LaunchedEffects watch for success/error messages from the ViewModel.
     * When a message appears, they display it via snackbar and immediately
     * clear it so it won't re-trigger on recomposition.
     */
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            /*
             * LazyColumn layout order:
             *   1. "Plans" header
             *   2. Plan cards (each with name, kcal, meal items, and "Apply" button)
             *   3. Empty-state placeholder (if no plans after loading)
             *   4. Bottom spacer for navigation-bar clearance
             */
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "header") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Plans",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (!state.isLoading && state.plans.isNotEmpty()) {
                    items(state.plans, key = { it.planId }) { plan ->
                        PlanCard(
                            plan = plan,
                            // Show spinner on the specific card being applied
                            isApplying = state.applyingPlanId == plan.planId,
                            // Disable ALL apply buttons while any plan is being applied
                            applyEnabled = state.applyingPlanId == null,
                            onApply = { viewModel.onApplyClicked(plan.planId) }
                        )
                    }
                }

                // Shown only after loading completes with zero plans
                if (!state.isLoading && state.plans.isEmpty()) {
                    item(key = "empty") {
                        EmptyPlansContent()
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Card for a single meal plan.
 *
 * Layout:
 *   - Header row: plan name (left) + total kcal badge (right)
 *   - Divider
 *   - Meal item rows (meal-type badge + meal name)
 *   - "Apply to today" button at the bottom
 *
 * [isApplying] shows a spinner inside the button for *this* card.
 * [applyEnabled] is false when *any* plan is being applied, locking all buttons.
 */
@Composable
private fun PlanCard(
    plan: PlanCardModel,
    isApplying: Boolean,
    applyEnabled: Boolean,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: plan name on the left, total kcal on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                plan.totalKcal?.let { kcal ->
                    Text(
                        text = "$kcal kcal",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // List of meals in this plan (e.g. Breakfast → Oatmeal, Lunch → Salad)
            plan.items.forEach { item ->
                PlanItemRow(item)
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            /*
             * "Apply to today" button.
             * Disabled while another plan is being applied (applyEnabled == false)
             * OR while this specific plan is mid-apply (isApplying == true).
             * Shows a spinner + "Applying…" during the async operation.
             */
            Button(
                onClick = onApply,
                enabled = applyEnabled && !isApplying,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Applying…")
                } else {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply to today")
                }
            }
        }
    }
}

/** A single row inside a plan card: meal-type badge on the left, meal name on the right. */
@Composable
private fun PlanItemRow(item: PlanItemUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MealTypeBadge(type = item.mealType)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = item.mealName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Small coloured chip that displays the meal type (Breakfast / Lunch / Dinner / Snack).
 * Uses [tertiaryContainer] colour to visually distinguish it from surrounding text.
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
 * Placeholder card shown when there are no meal plans in the database.
 * Displays a calendar icon and a message indicating no plans are available.
 */
@Composable
private fun EmptyPlansContent() {
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
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No plans available",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
