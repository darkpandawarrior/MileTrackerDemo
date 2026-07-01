package com.mileway.feature.logging.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mileway.core.ui.components.topbar.DepthAwareTopBar
import com.mileway.core.ui.theme.DesignTokens
import com.mileway.core.ui.theme.DesignTokens.NavigationDepth
import com.mileway.feature.logging.catalog.ExpenseCategoryCatalog
import com.mileway.feature.logging.model.DraftStatus
import com.mileway.feature.logging.model.ExpenseCategory
import com.mileway.feature.logging.model.ExpenseDraftRow
import com.mileway.feature.logging.viewmodel.ExpenseAction
import com.mileway.feature.logging.viewmodel.ExpenseViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    onBack: () -> Unit,
    onCategorySelected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = koinViewModel(),
) {
    val ui by viewModel.state.collectAsState()
    var bulkMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DepthAwareTopBar(
                title = "Add Expense",
                subtitle = if (bulkMode) "Bulk entry" else "Select a category",
                depth = NavigationDepth.LEVEL_1,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = DesignTokens.Spacing.l)
                    .navigationBarsPadding(),
        ) {
            Spacer(Modifier.height(DesignTokens.Spacing.l))

            ui.resumableDraft?.let { draft ->
                ResumeDraftCard(
                    merchantName = draft.merchantName,
                    onResume = {
                        viewModel.onAction(ExpenseAction.ResumeDraft)
                        onCategorySelected()
                    },
                    onDiscard = { viewModel.onAction(ExpenseAction.DiscardDraft) },
                )
                Spacer(Modifier.height(DesignTokens.Spacing.l))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (bulkMode) "Add multiple expenses at once" else "What type of expense is this?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                FilterChip(
                    selected = bulkMode,
                    onClick = { bulkMode = !bulkMode },
                    label = { Text("Bulk entry") },
                )
            }

            Spacer(Modifier.height(DesignTokens.Spacing.l))

            if (bulkMode) {
                // P2.4: local CSV/TSV bulk-import — parses the picked file's raw text into rows
                // appended alongside whatever the grid already has.
                val importCsv = rememberExpenseCsvImportLauncher { text -> viewModel.onAction(ExpenseAction.ImportCsv(text)) }
                OutlinedButton(onClick = importCsv, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                    Text(" Import CSV")
                }
                Spacer(Modifier.height(DesignTokens.Spacing.m))

                BulkDraftGrid(
                    rows = ui.rows,
                    onAddRow = { viewModel.onAction(ExpenseAction.AddDraftRow) },
                    onDuplicateRow = { id -> viewModel.onAction(ExpenseAction.DuplicateDraftRow(id)) },
                    onRemoveRow = { id -> viewModel.onAction(ExpenseAction.RemoveDraftRow(id)) },
                    onMerchantChange = { id, value ->
                        viewModel.onAction(ExpenseAction.UpdateDraftRow(id) { it.copy(merchantName = value) })
                    },
                    onAmountChange = { id, value ->
                        viewModel.onAction(ExpenseAction.UpdateDraftRow(id) { it.copy(amountText = value) })
                    },
                    onCategoryChange = { id, category ->
                        viewModel.onAction(ExpenseAction.UpdateDraftRow(id) { it.copy(category = category) })
                    },
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
                    contentPadding = PaddingValues(bottom = DesignTokens.Spacing.xl),
                ) {
                    items(ExpenseCategoryCatalog.default()) { categoryDef ->
                        CategoryTile(
                            category = categoryDef.category,
                            onClick = {
                                viewModel.onAction(ExpenseAction.SelectCategory(categoryDef.category))
                                onCategorySelected()
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * P2.1: multi-row draft grid for bulk expense entry — the local, offline equivalent of the
 * reference app's bulk-entry screen structure (add/duplicate/remove rows), rendered with
 * Mileway's own card + [DesignTokens] styling rather than a port of the reference UI.
 */
@Composable
private fun BulkDraftGrid(
    rows: List<ExpenseDraftRow>,
    onAddRow: () -> Unit,
    onDuplicateRow: (String) -> Unit,
    onRemoveRow: (String) -> Unit,
    onMerchantChange: (String, String) -> Unit,
    onAmountChange: (String, String) -> Unit,
    onCategoryChange: (String, ExpenseCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.m),
        contentPadding = PaddingValues(bottom = DesignTokens.Spacing.xl),
    ) {
        items(rows, key = { it.id }) { row ->
            DraftRowCard(
                row = row,
                canRemove = rows.size > 1,
                onDuplicate = { onDuplicateRow(row.id) },
                onRemove = { onRemoveRow(row.id) },
                onMerchantChange = { value -> onMerchantChange(row.id, value) },
                onAmountChange = { value -> onAmountChange(row.id, value) },
                onCategoryChange = { category -> onCategoryChange(row.id, category) },
            )
        }
        item {
            OutlinedButton(onClick = onAddRow, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(" Add row")
            }
        }
    }
}

@Composable
private fun DraftRowCard(
    row: ExpenseDraftRow,
    canRemove: Boolean,
    onDuplicate: () -> Unit,
    onRemove: () -> Unit,
    onMerchantChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = row.category?.label ?: "Select category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Row {
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate row")
                    }
                    if (canRemove) {
                        IconButton(onClick = onRemove) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove row")
                        }
                    }
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.s))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s),
            ) {
                ExpenseCategoryCatalog.default().forEach { categoryDef ->
                    FilterChip(
                        selected = row.category == categoryDef.category,
                        onClick = { onCategoryChange(categoryDef.category) },
                        label = { Text(categoryDef.category.label) },
                    )
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.m))

            OutlinedTextField(
                value = row.merchantName,
                onValueChange = onMerchantChange,
                label = { Text("Merchant") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(DesignTokens.Spacing.s))

            OutlinedTextField(
                value = row.amountText,
                onValueChange = onAmountChange,
                label = { Text("Amount") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (row.status == DraftStatus.ERROR) {
                Spacer(Modifier.height(DesignTokens.Spacing.s))
                Text(
                    text = "This row needs attention before submitting",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

/**
 * P1.5: offers to resume a Room-persisted draft found on entry (kill+relaunch survived). Never
 * blocks starting a fresh expense — dismissing keeps the draft persisted, discarding clears it.
 */
@Composable
private fun ResumeDraftCard(
    merchantName: String,
    onResume: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DesignTokens.Shape.roundedMd,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(DesignTokens.Spacing.l)) {
            Text(
                text = "Resume draft?",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(DesignTokens.Spacing.s))
            Text(
                text = if (merchantName.isNotBlank()) "You have an unsaved expense for \"$merchantName\"" else "You have an unsaved expense in progress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(DesignTokens.Spacing.m))
            Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.s)) {
                OutlinedButton(onClick = onResume) { Text("Resume") }
                TextButton(onClick = onDiscard) { Text("Discard") }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: ExpenseCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(DesignTokens.Shape.roundedMd)
                .clickable(onClick = onClick),
        shape = DesignTokens.Shape.roundedMd,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )
        }
    }
}
