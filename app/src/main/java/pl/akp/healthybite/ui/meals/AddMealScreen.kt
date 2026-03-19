package pl.akp.healthybite.ui.meals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.akp.healthybite.data.db.entity.MealTemplateEntity
import pl.akp.healthybite.domain.model.MealType

/**
 * Add Meal screen – lets the user log a new meal for today.
 *
 * The top section selects the [MealType]; a mode toggle switches between
 * **template** (pick from a list) and **custom** (manual entry with
 * name, calories, and optional macros). Navigates back on successful save.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    viewModel: AddMealViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // When the ViewModel signals a successful save, navigate back to the previous screen.
    // LaunchedEffect re-runs whenever state.saved changes; once true it fires onBack().
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    // Scaffold provides the top app bar (with a back arrow) and inset-aware padding.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add meal") },
                navigationIcon = {
                    // Back button returns to the previous screen without saving.
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Thin progress bar at the top while a save is in progress.
            if (state.isSaving) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                MealTypeSelector(
                    selected = state.selectedType,
                    onSelected = viewModel::onTypeSelected
                )

                Spacer(modifier = Modifier.height(12.dp))

                ModeSelector(
                    mode = state.mode,
                    onModeChanged = viewModel::onModeChanged
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Swap between the template list or the custom form based on the active mode.
                when (state.mode) {
                    AddMealMode.TEMPLATE -> TemplateContent(
                        state = state,
                        onTemplateSelected = viewModel::onTemplateSelected,
                        onSave = viewModel::onSaveClicked,
                        modifier = Modifier.weight(1f)
                    )
                    AddMealMode.CUSTOM -> CustomContent(
                        state = state,
                        onNameChanged = viewModel::onCustomNameChanged,
                        onCaloriesChanged = viewModel::onCustomCaloriesChanged,
                        onProteinChanged = viewModel::onCustomProteinChanged,
                        onFatChanged = viewModel::onCustomFatChanged,
                        onCarbsChanged = viewModel::onCustomCarbsChanged,
                        onSave = viewModel::onSaveClicked,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Segmented button row for choosing a MealType (Breakfast / Lunch / Dinner / Snack).
 * Only one segment can be active at a time. Tapping a segment calls [onSelected],
 * which triggers the ViewModel to reload templates for that type.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTypeSelector(
    selected: MealType,
    onSelected: (MealType) -> Unit
) {
    val types = MealType.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index, types.size),
                selected = type == selected,
                onClick = { onSelected(type) }
            ) {
                Text(type.label())
            }
        }
    }
}

/**
 * A pair of FilterChips that toggle between "From templates" and "Custom" mode.
 * The selected chip is visually highlighted. Changing the mode clears validation errors
 * in the ViewModel so the user starts fresh.
 */
@Composable
private fun ModeSelector(
    mode: AddMealMode,
    onModeChanged: (AddMealMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = mode == AddMealMode.TEMPLATE,
            onClick = { onModeChanged(AddMealMode.TEMPLATE) },
            label = { Text("From templates") }
        )
        FilterChip(
            selected = mode == AddMealMode.CUSTOM,
            onClick = { onModeChanged(AddMealMode.CUSTOM) },
            label = { Text("Custom") }
        )
    }
}

/**
 * Content shown when the user is in Template mode.
 *
 * Displays a loading spinner while templates are fetched, an empty-state message if none
 * exist for the selected type, or a scrollable list of [TemplateRow] items with radio-button
 * selection. The Save button at the bottom is enabled via [AddMealUiState.submitEnabled].
 */
@Composable
private fun TemplateContent(
    state: AddMealUiState,
    onTemplateSelected: (Long) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.templates.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No templates for ${state.selectedType.label()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.templates, key = { it.id }) { template ->
                    TemplateRow(
                        template = template,
                        isSelected = template.id == state.selectedTemplateId,
                        onClick = { onTemplateSelected(template.id) }
                    )
                }
            }
        }

        state.errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save button — enabled only when a template is selected and no save is in progress.
        Button(
            onClick = onSave,
            enabled = state.submitEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

/**
 * A single row in the template list.
 *
 * Shows a radio button, the template name, and a compact macro summary
 * (kcal · P · F · C). The card background switches to primaryContainer when selected
 * to give clear visual feedback.
 */
@Composable
private fun TemplateRow(
    template: MealTemplateEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = template.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${template.kcal} kcal · P ${template.proteinG}g · F ${template.fatG}g · C ${template.carbsG}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Content shown when the user is in Custom mode.
 *
 * Provides a vertically scrollable form with:
 *   - Meal name (required text field)
 *   - Calories (required numeric field)
 *   - Protein, Fat, Carbs (optional numeric fields)
 *
 * Each field performs live validation on every keystroke via the ViewModel's
 * onCustom*Changed() callbacks. The Save button is enabled by [AddMealUiState.submitEnabled].
 */
@Composable
private fun CustomContent(
    state: AddMealUiState,
    onNameChanged: (String) -> Unit,
    onCaloriesChanged: (String) -> Unit,
    onProteinChanged: (String) -> Unit,
    onFatChanged: (String) -> Unit,
    onCarbsChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = state.customName,
            onValueChange = onNameChanged,
            label = { Text("Meal name") },
            isError = state.customNameError != null,
            supportingText = state.customNameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.customCalories,
            onValueChange = onCaloriesChanged,
            label = { Text("Calories (kcal)") },
            isError = state.caloriesError != null,
            supportingText = state.caloriesError?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.customProtein,
            onValueChange = onProteinChanged,
            label = { Text("Protein (g) — optional") },
            isError = state.proteinError != null,
            supportingText = state.proteinError?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.customFat,
            onValueChange = onFatChanged,
            label = { Text("Fat (g) — optional") },
            isError = state.fatError != null,
            supportingText = state.fatError?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.customCarbs,
            onValueChange = onCarbsChanged,
            label = { Text("Carbs (g) — optional") },
            isError = state.carbsError != null,
            supportingText = state.carbsError?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save button — enabled when name + calories are filled and no validation errors exist.
        Button(
            onClick = onSave,
            enabled = state.submitEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** Converts a [MealType] enum constant to a user-friendly display string for buttons and labels. */
private fun MealType.label(): String = when (this) {
    MealType.BREAKFAST -> "Breakfast"
    MealType.LUNCH -> "Lunch"
    MealType.DINNER -> "Dinner"
    MealType.SNACK -> "Snack"
}
