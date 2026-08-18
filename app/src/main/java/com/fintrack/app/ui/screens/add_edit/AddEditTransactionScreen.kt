package com.fintrack.app.ui.screens.add_edit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.app.R
import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.model.CategoryType
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticGreen
import com.fintrack.app.ui.theme.SemanticRed
import com.fintrack.app.ui.util.CategoryIconHelper
import com.fintrack.app.ui.viewmodel.AppViewModelProvider
import java.time.format.DateTimeFormatter

/**
 * Stateful entry composable for Add/Edit Transaction Screen.
 */
@Composable
fun AddEditTransactionScreen(
    transactionId: Long? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditTransactionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(transactionId) {
        viewModel.initForTransaction(transactionId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditUiEvent.NavigateBack -> onNavigateBack()
                is AddEditUiEvent.ShowError -> { /* handle error */ }
            }
        }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val onDateClick = {
        val currentDt = uiState.dateTime
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.onDateTimeChange(
                    currentDt.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth)
                )
            },
            currentDt.year,
            currentDt.monthValue - 1,
            currentDt.dayOfMonth
        ).show()
    }

    val onTimeClick = {
        val currentDt = uiState.dateTime
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                viewModel.onDateTimeChange(
                    currentDt.withHour(hourOfDay).withMinute(minute)
                )
            },
            currentDt.hour,
            currentDt.minute,
            true
        ).show()
    }

    AddEditTransactionScreenContent(
        isEditMode = uiState.isEditing,
        selectedType = uiState.type,
        onTypeSelected = viewModel::onTypeChange,
        amountText = uiState.amountInput,
        amountError = uiState.amountError,
        onAmountChange = viewModel::onAmountChange,
        categories = uiState.categories,
        selectedCategory = uiState.selectedCategory,
        categoryError = uiState.categoryError,
        onCategorySelected = viewModel::onCategorySelect,
        dateFormatted = uiState.dateTime.format(dateFormatter),
        timeFormatted = uiState.dateTime.format(timeFormatter),
        onDateClick = onDateClick,
        onTimeClick = onTimeClick,
        noteText = uiState.note,
        onNoteChange = viewModel::onNoteChange,
        isSubmitting = uiState.isSubmitting,
        isLoading = uiState.isLoading,
        generalError = uiState.generalError,
        onSaveClick = viewModel::saveTransaction,
        onCancelClick = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Stateless pure UI component for Add/Edit Transaction Screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionScreenContent(
    isEditMode: Boolean,
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    amountText: String,
    amountError: String?,
    onAmountChange: (String) -> Unit,
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    categoryError: String?,
    onCategorySelected: (CategoryEntity) -> Unit,
    dateFormatted: String,
    timeFormatted: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    noteText: String,
    onNoteChange: (String) -> Unit,
    isSubmitting: Boolean,
    isLoading: Boolean,
    generalError: String?,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancelClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = if (isEditMode) stringResource(R.string.edit_title) else stringResource(R.string.add_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isEditMode) stringResource(R.string.edit_subtitle) else stringResource(R.string.add_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Error Banner
            if (generalError != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SemanticRed.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = generalError,
                            style = MaterialTheme.typography.bodySmall,
                            color = SemanticRed,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // 1. Transaction Type Toggle (Segmented Button)
            item {
                val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    types.forEachIndexed { index, type ->
                        val isSelected = selectedType == type
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                            onClick = { onTypeSelected(type) },
                            selected = isSelected,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = if (type == TransactionType.EXPENSE) {
                                    SemanticRed.copy(alpha = 0.15f)
                                } else {
                                    SemanticGreen.copy(alpha = 0.15f)
                                },
                                activeContentColor = if (type == TransactionType.EXPENSE) SemanticRed else SemanticGreen
                            )
                        ) {
                            Text(
                                text = if (type == TransactionType.EXPENSE) {
                                    stringResource(R.string.add_type_expense)
                                } else {
                                    stringResource(R.string.add_type_income)
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 2. Amount Input Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.add_amount_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = onAmountChange,
                            placeholder = {
                                Text(
                                    text = "0",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (selectedType == TransactionType.EXPENSE) SemanticRed else SemanticGreen
                            ),
                            isError = amountError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (selectedType == TransactionType.EXPENSE) SemanticRed else SemanticGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (amountError != null) {
                            Text(
                                text = stringResource(R.string.add_error_amount),
                                style = MaterialTheme.typography.bodySmall,
                                color = SemanticRed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. Category Selector Card
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.add_category_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            if (selectedCategory != null) {
                                Text(
                                    text = selectedCategory.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (categories.isEmpty()) {
                            Text(
                                text = stringResource(R.string.add_category_loading),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { category ->
                                    val isSelected = selectedCategory?.id == category.id
                                    val catColor = CategoryIconHelper.parseColor(category.colorKey)
                                    val catIcon = CategoryIconHelper.getIconByName(category.iconKey)

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) catColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable { onCategorySelected(category) }
                                            .then(
                                                if (isSelected) Modifier.border(2.dp, catColor, RoundedCornerShape(20.dp))
                                                else Modifier
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = catIcon,
                                                contentDescription = category.name,
                                                tint = if (isSelected) Color.White else catColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = category.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (categoryError != null) {
                            Text(
                                text = stringResource(R.string.add_error_category),
                                style = MaterialTheme.typography.bodySmall,
                                color = SemanticRed,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            // 4. Date & Time Selection Card
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.add_time_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Date Selector
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onDateClick() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = stringResource(R.string.detail_date),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = dateFormatted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Time Selector
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onTimeClick() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = stringResource(R.string.detail_time),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = timeFormatted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Note Input Card
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.add_note_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = noteText,
                            onValueChange = onNoteChange,
                            placeholder = { Text(stringResource(R.string.add_note_hint)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = stringResource(R.string.detail_note),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 6. Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Button(
                        onClick = onSaveClick,
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .weight(2.2f)
                            .height(50.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEditMode) stringResource(R.string.add_update) else stringResource(R.string.add_save),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AddEditTransactionScreenPreview() {
    FinTrackTheme {
        AddEditTransactionScreenContent(
            isEditMode = false,
            selectedType = TransactionType.EXPENSE,
            onTypeSelected = {},
            amountText = "50000",
            amountError = null,
            onAmountChange = {},
            categories = listOf(
                CategoryEntity(1L, "Ăn uống", "fastfood", "#FFA000", CategoryType.EXPENSE, isDefault = true),
                CategoryEntity(2L, "Mua sắm", "shopping_cart", "#7B1FA2", CategoryType.EXPENSE, isDefault = true)
            ),
            selectedCategory = CategoryEntity(1L, "Ăn uống", "fastfood", "#FFA000", CategoryType.EXPENSE, isDefault = true),
            categoryError = null,
            onCategorySelected = {},
            dateFormatted = "15/08/2026",
            timeFormatted = "12:30",
            onDateClick = {},
            onTimeClick = {},
            noteText = "Ăn trưa bún bò",
            onNoteChange = {},
            isSubmitting = false,
            isLoading = false,
            generalError = null,
            onSaveClick = {},
            onCancelClick = {}
        )
    }
}
