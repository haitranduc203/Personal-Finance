package com.fintrack.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.app.FinTrackApplication
import com.fintrack.app.R
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.preferences.UserPreferences
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticGreen
import com.fintrack.app.ui.theme.SemanticRed
import com.fintrack.app.ui.util.CategoryIconHelper
import com.fintrack.app.ui.util.CurrencyFormatter
import com.fintrack.app.ui.util.toLocalDateTime
import com.fintrack.app.ui.viewmodel.AppViewModelProvider
import java.time.format.DateTimeFormatter

data class TransactionDetailUiModel(
    val id: Long,
    val amountFormatted: String,
    val rawAmountFormatted: String = "",
    val isExpense: Boolean,
    val categoryName: String,
    val categoryIcon: ImageVector,
    val categoryColor: Color,
    val dateFormatted: String,
    val timeFormatted: String,
    val note: String
)

/**
 * Stateful entry composable for Transaction Detail Screen.
 */
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val app = LocalContext.current.applicationContext as? FinTrackApplication
    val prefs by (app?.preferencesRepository?.userPreferencesFlow
        ?.collectAsStateWithLifecycle(UserPreferences())
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(UserPreferences()) })

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailUiEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val defaultNoNote = stringResource(R.string.detail_no_note)
    val uiModel = uiState.transaction?.let { txWithCat ->
        val isExpense = txWithCat.transaction.type == TransactionType.EXPENSE
        val amountStr = CurrencyFormatter.format(
            amount = txWithCat.transaction.amount,
            currency = prefs.currency,
            withSign = true,
            isExpense = isExpense,
            isIncome = !isExpense
        )
        val rawAmountStr = CurrencyFormatter.format(
            amount = txWithCat.transaction.amount,
            currency = prefs.currency,
            withSign = false
        )
        val dt = txWithCat.transaction.transactionDate.toLocalDateTime()

        TransactionDetailUiModel(
            id = txWithCat.transaction.id,
            amountFormatted = amountStr,
            rawAmountFormatted = rawAmountStr,
            isExpense = isExpense,
            categoryName = txWithCat.category.name,
            categoryIcon = CategoryIconHelper.getIconByName(txWithCat.category.iconKey),
            categoryColor = CategoryIconHelper.parseColor(txWithCat.category.colorKey),
            dateFormatted = dt.format(dateFormatter),
            timeFormatted = dt.format(timeFormatter),
            note = txWithCat.transaction.note?.ifBlank { null } ?: defaultNoNote
        )
    }

    TransactionDetailScreenContent(
        transaction = uiModel,
        isLoading = uiState.isLoading,
        isDeleting = uiState.isDeleting,
        errorMessage = uiState.errorMessage,
        showDeleteConfirmDialog = uiState.showDeleteConfirmDialog,
        onNavigateBack = onNavigateBack,
        onEditClick = { onNavigateToEdit(transactionId) },
        onDeleteClick = viewModel::showDeleteDialog,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        modifier = modifier
    )
}

/**
 * Stateless pure UI component for Transaction Detail Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreenContent(
    transaction: TransactionDetailUiModel?,
    isLoading: Boolean,
    isDeleting: Boolean,
    errorMessage: String?,
    showDeleteConfirmDialog: Boolean,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 6.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.detail_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (transaction != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.action_edit),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = SemanticRed
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }

        if (errorMessage != null || transaction == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage ?: stringResource(R.string.add_error_not_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text(stringResource(R.string.action_back))
                    }
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Amount Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (transaction.isExpense) {
                            SemanticRed.copy(alpha = 0.08f)
                        } else {
                            SemanticGreen.copy(alpha = 0.08f)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Category Icon Badge
                        Surface(
                            shape = CircleShape,
                            color = transaction.categoryColor,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = transaction.categoryIcon,
                                    contentDescription = transaction.categoryName,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Name Tag
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = transaction.categoryName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = transaction.categoryColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Amount Value
                        Text(
                            text = transaction.amountFormatted,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.isExpense) SemanticRed else SemanticGreen
                        )

                        Text(
                            text = if (transaction.isExpense) stringResource(R.string.detail_type_expense) else stringResource(R.string.detail_type_income),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Details Metadata Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailInfoRow(
                            icon = Icons.Default.CalendarMonth,
                            label = stringResource(R.string.detail_date),
                            value = transaction.dateFormatted
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        DetailInfoRow(
                            icon = Icons.Default.Schedule,
                            label = stringResource(R.string.detail_time),
                            value = transaction.timeFormatted
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        DetailInfoRow(
                            icon = Icons.Default.Category,
                            label = stringResource(R.string.detail_type),
                            value = if (transaction.isExpense) stringResource(R.string.detail_type_expense) else stringResource(R.string.detail_type_income)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        DetailInfoRow(
                            icon = Icons.Default.Description,
                            label = stringResource(R.string.detail_note),
                            value = transaction.note
                        )
                    }
                }
            }

            // 3. Action Buttons Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDeleteClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = SemanticRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action_delete), color = SemanticRed, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action_edit), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = {
                Text(
                    text = stringResource(R.string.detail_delete_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val displayAmount = transaction?.rawAmountFormatted?.ifBlank { null }
                    ?: transaction?.amountFormatted?.removePrefix("-")?.removePrefix("+")
                    ?: ""
                Text(
                    text = stringResource(R.string.detail_delete_msg, transaction?.categoryName.orEmpty(), displayAmount),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDelete,
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticRed)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.action_confirm))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@PreviewLightDark
@Composable
private fun TransactionDetailScreenPreview() {
    FinTrackTheme {
        TransactionDetailScreenContent(
            transaction = TransactionDetailUiModel(
                id = 1L,
                amountFormatted = "-50.000 ₫",
                rawAmountFormatted = "50.000 ₫",
                isExpense = true,
                categoryName = "Ăn uống",
                categoryIcon = Icons.Default.Fastfood,
                categoryColor = Color(0xFFFFA000),
                dateFormatted = "15/08/2026",
                timeFormatted = "12:30",
                note = "Ăn trưa bún bò Huế"
            ),
            isLoading = false,
            isDeleting = false,
            errorMessage = null,
            showDeleteConfirmDialog = false,
            onNavigateBack = {},
            onEditClick = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onDismissDeleteDialog = {}
        )
    }
}
