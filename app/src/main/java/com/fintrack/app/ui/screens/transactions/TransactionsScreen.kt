package com.fintrack.app.ui.screens.transactions

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.app.R
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticGreen
import com.fintrack.app.ui.theme.SemanticRed
import com.fintrack.app.ui.util.CategoryIconHelper
import com.fintrack.app.ui.util.CurrencyFormatter
import com.fintrack.app.ui.viewmodel.AppViewModelProvider
import java.time.LocalDate

/**
 * Stateful entry composable for Transactions Screen with Period Navigator.
 */
@Composable
fun TransactionsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPeriodSelectorDialog by remember { mutableStateOf(false) }

    val totalIncomeFormatted = CurrencyFormatter.format(
        amount = uiState.totalIncome,
        currency = uiState.currency,
        withSign = true,
        isIncome = true
    )
    val totalExpenseFormatted = CurrencyFormatter.format(
        amount = uiState.totalExpense,
        currency = uiState.currency,
        withSign = true,
        isExpense = true
    )

    TransactionsScreenContent(
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        selectedFilter = uiState.selectedFilter,
        onFilterSelected = viewModel::onFilterSelected,
        currentPeriodTitle = uiState.currentPeriodTitle,
        canNavigatePeriod = uiState.canNavigatePeriod,
        onPreviousPeriod = viewModel::onPreviousPeriod,
        onNextPeriod = viewModel::onNextPeriod,
        onPeriodClick = { showPeriodSelectorDialog = true },
        transactionGroups = uiState.transactionGroups,
        totalCount = uiState.totalCount,
        totalIncomeFormatted = totalIncomeFormatted,
        totalExpenseFormatted = totalExpenseFormatted,
        isLoading = uiState.isLoading,
        onResetFilter = viewModel::resetToDefaultMonth,
        onTransactionClick = onNavigateToDetail,
        onAddTransactionClick = onNavigateToAddTransaction,
        modifier = modifier
    )

    // Period Mode Selector Dialog
    if (showPeriodSelectorDialog) {
        PeriodSelectorDialog(
            onDismiss = { showPeriodSelectorDialog = false },
            onSelectThisMonth = {
                showPeriodSelectorDialog = false
                viewModel.onSelectThisMonth()
            },
            onSelectToday = {
                showPeriodSelectorDialog = false
                viewModel.onSelectToday()
            },
            onSelectThisWeek = {
                showPeriodSelectorDialog = false
                viewModel.onSelectThisWeek()
            },
            onSelectThisYear = {
                showPeriodSelectorDialog = false
                viewModel.onSelectThisYear()
            },
            onSelectAllTime = {
                showPeriodSelectorDialog = false
                viewModel.onSelectAllTime()
            },
            onSelectSpecificDate = {
                showPeriodSelectorDialog = false
                val now = LocalDate.now()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selected = LocalDate.of(year, month + 1, dayOfMonth)
                        viewModel.onSpecificDateSelected(selected)
                    },
                    now.year,
                    now.monthValue - 1,
                    now.dayOfMonth
                ).show()
            },
            onSelectDateRange = {
                showPeriodSelectorDialog = false
                val now = LocalDate.now()
                DatePickerDialog(
                    context,
                    { _, startYear, startMonth, startDay ->
                        val startDate = LocalDate.of(startYear, startMonth + 1, startDay)
                        DatePickerDialog(
                            context,
                            { _, endYear, endMonth, endDay ->
                                val endDate = LocalDate.of(endYear, endMonth + 1, endDay)
                                viewModel.onDateRangeSelected(startDate, endDate)
                            },
                            startDate.year,
                            startDate.monthValue - 1,
                            startDate.dayOfMonth
                        ).apply {
                            setTitle("Chọn ngày kết thúc")
                        }.show()
                    },
                    now.year,
                    now.monthValue - 1,
                    now.dayOfMonth
                ).apply {
                    setTitle("Chọn ngày bắt đầu")
                }.show()
            }
        )
    }
}

/**
 * Stateless pure UI component for Transactions Screen with Period Navigator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreenContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
    currentPeriodTitle: String,
    canNavigatePeriod: Boolean,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onPeriodClick: () -> Unit,
    transactionGroups: List<TransactionGroupUi>,
    totalCount: Int,
    totalIncomeFormatted: String,
    totalExpenseFormatted: String,
    isLoading: Boolean,
    onResetFilter: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeFilters = listOf(
        TransactionFilter.ALL,
        TransactionFilter.EXPENSE,
        TransactionFilter.INCOME
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.transactions_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.transactions_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { /* Notification action */ },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.notif_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2. Search Bar & Type Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.transactions_search_hint),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.transactions_search_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.transactions_search_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Type Filter Chips Row (Tất cả | Khoản chi | Khoản thu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                typeFilters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val chipTitle = when (filter) {
                        TransactionFilter.ALL -> stringResource(R.string.transactions_filter_all)
                        TransactionFilter.EXPENSE -> stringResource(R.string.transactions_filter_expense)
                        TransactionFilter.INCOME -> stringResource(R.string.transactions_filter_income)
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter) },
                        label = {
                            Text(
                                text = chipTitle,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Period Navigator Bar (‹ [📅 Tháng 8, 2026 ▾] ›)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous Button
                IconButton(
                    onClick = onPreviousPeriod,
                    enabled = canNavigatePeriod,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Kỳ trước",
                        tint = if (canNavigatePeriod) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Interactive Period Selector Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onPeriodClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = currentPeriodTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Đổi kỳ thời gian",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Next Button
                IconButton(
                    onClick = onNextPeriod,
                    enabled = canNavigatePeriod,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Kỳ sau",
                        tint = if (canNavigatePeriod) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 4. Summary Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.transactions_total_count, totalCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Thu: $totalIncomeFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SemanticGreen
                    )
                    Text(
                        text = "Chi: $totalExpenseFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SemanticRed
                    )
                }
            }
        }

        // 5. Transaction List or Empty State
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else if (transactionGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.transactions_empty_search),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.transactions_empty_search_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onResetFilter) {
                        Text("Về tháng hiện tại")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                transactionGroups.forEach { group ->
                    item(key = "header_${group.dateHeader}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.dateHeader,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = group.dailyNetFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(
                        items = group.transactions,
                        key = { "tx_${it.id}" }
                    ) { item ->
                        TransactionItemCard(
                            item = item,
                            onClick = { onTransactionClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean Period Mode Selector Dialog.
 */
@Composable
fun PeriodSelectorDialog(
    onDismiss: () -> Unit,
    onSelectThisMonth: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectThisWeek: () -> Unit,
    onSelectThisYear: () -> Unit,
    onSelectAllTime: () -> Unit,
    onSelectSpecificDate: () -> Unit,
    onSelectDateRange: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Chọn kỳ thời gian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PeriodOptionItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Theo Tháng (Mặc định)",
                    subtitle = "Xem thu chi theo từng tháng (lướt ‹ › để chuyển tháng)",
                    onClick = onSelectThisMonth
                )
                PeriodOptionItem(
                    icon = Icons.Default.Today,
                    title = "Hôm nay",
                    subtitle = "Chỉ xem các giao dịch phát sinh hôm nay",
                    onClick = onSelectToday
                )
                PeriodOptionItem(
                    icon = Icons.Default.ViewWeek,
                    title = "Theo Tuần",
                    subtitle = "Xem thu chi từ Thứ 2 đến Chủ nhật",
                    onClick = onSelectThisWeek
                )
                PeriodOptionItem(
                    icon = Icons.Default.DateRange,
                    title = "Theo Năm",
                    subtitle = "Xem tổng kết thu chi của toàn bộ năm",
                    onClick = onSelectThisYear
                )
                PeriodOptionItem(
                    icon = Icons.Default.Public,
                    title = "Toàn bộ thời gian",
                    subtitle = "Hiển thị tất cả lịch sử giao dịch từ trước đến nay",
                    onClick = onSelectAllTime
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                PeriodOptionItem(
                    icon = Icons.Default.CalendarToday,
                    title = "Chọn một ngày cụ thể...",
                    subtitle = "Mở lịch chọn 1 ngày bất kỳ",
                    onClick = onSelectSpecificDate
                )
                PeriodOptionItem(
                    icon = Icons.Default.DateRange,
                    title = "Khoảng ngày tùy chọn...",
                    subtitle = "Lọc giao dịch từ ngày A đến ngày B",
                    onClick = onSelectDateRange
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun PeriodOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Clean transaction card item rendered in the transactions list.
 */
@Composable
fun TransactionItemCard(
    item: TransactionItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = CategoryIconHelper.parseColor(item.categoryColorKey)
    val categoryIcon = CategoryIconHelper.getIconByName(item.categoryIconKey)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon Badge
                Surface(
                    shape = CircleShape,
                    color = categoryColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = item.categoryName,
                            tint = categoryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Title, Category, and Time
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.categoryName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.timeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Amount
            Text(
                text = item.amountFormatted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.isExpense) SemanticRed else SemanticGreen
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TransactionsScreenPreview() {
    FinTrackTheme {
        TransactionsScreenContent(
            searchQuery = "",
            onSearchQueryChange = {},
            selectedFilter = TransactionFilter.ALL,
            onFilterSelected = {},
            currentPeriodTitle = "Tháng 8, 2026",
            canNavigatePeriod = true,
            onPreviousPeriod = {},
            onNextPeriod = {},
            onPeriodClick = {},
            transactionGroups = emptyList(),
            totalCount = 0,
            totalIncomeFormatted = "+0 ₫",
            totalExpenseFormatted = "-0 ₫",
            isLoading = false,
            onResetFilter = {},
            onTransactionClick = {},
            onAddTransactionClick = {}
        )
    }
}
