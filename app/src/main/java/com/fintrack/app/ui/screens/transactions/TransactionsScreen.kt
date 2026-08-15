package com.fintrack.app.ui.screens.transactions

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticGreen
import com.fintrack.app.ui.theme.SemanticRed
import com.fintrack.app.ui.util.CategoryIconHelper
import com.fintrack.app.ui.viewmodel.AppViewModelProvider
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TransactionItemUi(
    val id: Long,
    val title: String,
    val categoryName: String,
    val categoryIcon: ImageVector,
    val categoryColor: Color,
    val amountFormatted: String,
    val isExpense: Boolean,
    val timeFormatted: String,
    val date: LocalDate
)

data class TransactionGroupUi(
    val dateHeader: String,
    val dailyNetFormatted: String,
    val transactions: List<TransactionItemUi>
)

/**
 * Stateful entry composable for Transactions Screen.
 */
@Composable
fun TransactionsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val decimalFormat = DecimalFormat("#,###")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    // Map list to UI models and group by date
    val groups = uiState.filteredTransactions
        .map { txWithCat ->
            val isExpense = txWithCat.transaction.type == TransactionType.EXPENSE
            val prefix = if (isExpense) "-" else "+"
            val amountStr = "$prefix${decimalFormat.format(txWithCat.transaction.amount)} ₫"
            val dt = java.time.Instant.ofEpochMilli(txWithCat.transaction.transactionDate)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()

            TransactionItemUi(
                id = txWithCat.transaction.id,
                title = txWithCat.transaction.note ?: txWithCat.category.name,
                categoryName = txWithCat.category.name,
                categoryIcon = CategoryIconHelper.getIconByName(txWithCat.category.iconKey),
                categoryColor = CategoryIconHelper.parseColor(txWithCat.category.colorKey),
                amountFormatted = amountStr,
                isExpense = isExpense,
                timeFormatted = dt.format(timeFormatter),
                date = dt.toLocalDate()
            )
        }
        .groupBy { it.date }
        .map { (date, items) ->
            val headerTitle = when (date) {
                today -> "Hôm nay, ${date.format(DateTimeFormatter.ofPattern("dd 'Th'MM"))}"
                yesterday -> "Hôm qua, ${date.format(DateTimeFormatter.ofPattern("dd 'Th'MM"))}"
                else -> date.format(DateTimeFormatter.ofPattern("dd 'Th'MM, yyyy"))
            }

            var netDaily = 0.0
            items.forEach {
                val rawAmount = it.amountFormatted.replace(".", "").replace("+", "").replace("-", "").replace(" ₫", "").trim().toDoubleOrNull() ?: 0.0
                if (it.isExpense) netDaily -= rawAmount else netDaily += rawAmount
            }
            val netPrefix = if (netDaily >= 0) "+" else "-"
            val netStr = "$netPrefix${decimalFormat.format(Math.abs(netDaily))} ₫"

            TransactionGroupUi(
                dateHeader = headerTitle,
                dailyNetFormatted = netStr,
                transactions = items
            )
        }

    val totalIncomeFormatted = "+${decimalFormat.format(uiState.totalIncome)} ₫"
    val totalExpenseFormatted = "-${decimalFormat.format(uiState.totalExpense)} ₫"

    TransactionsScreenContent(
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        selectedFilter = uiState.selectedFilter,
        onFilterSelected = viewModel::onFilterSelected,
        transactionGroups = groups,
        totalCount = uiState.totalCount,
        totalIncomeFormatted = totalIncomeFormatted,
        totalExpenseFormatted = totalExpenseFormatted,
        isLoading = uiState.isLoading,
        onTransactionClick = onNavigateToDetail,
        onAddTransactionClick = onNavigateToAddTransaction,
        modifier = modifier
    )
}

/**
 * Stateless pure UI component for Transactions Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreenContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
    transactionGroups: List<TransactionGroupUi>,
    totalCount: Int,
    totalIncomeFormatted: String,
    totalExpenseFormatted: String,
    isLoading: Boolean,
    onTransactionClick: (Long) -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = TransactionFilter.entries

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Compact Header — Matches Home Screen Design
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Lịch sử thu chi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sổ giao dịch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Chọn tháng",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Tháng 8, 2026",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = { /* Notification action */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Thông báo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Search and Filter Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Compact Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
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
                        contentDescription = "Tìm kiếm",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
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
                                    text = "Tìm kiếm giao dịch...",
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
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Xóa tìm kiếm",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter) },
                        label = {
                            Text(
                                text = filter.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Summary Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
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
                    text = "Tổng $totalCount giao dịch",
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

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }

        if (transactionGroups.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Trống",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = if (searchQuery.isNotEmpty()) "Không tìm thấy giao dịch" else "Chưa có giao dịch nào",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (searchQuery.isNotEmpty()) "Hãy thử từ khóa khác hoặc xóa bộ lọc" else "Bắt đầu ghi chép khoản thu hoặc chi tiêu đầu tiên của bạn!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (searchQuery.isEmpty()) {
                        Button(
                            onClick = onAddTransactionClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Thêm giao dịch", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Thêm giao dịch ngay")
                        }
                    }
                }
            }
            return
        }

        // Transactions List by Date
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            transactionGroups.forEach { group ->
                item(key = group.dateHeader) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = group.dateHeader,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = group.dailyNetFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(group.transactions, key = { it.id }) { tx ->
                    TransactionListItemCard(
                        transaction = tx,
                        onClick = { onTransactionClick(tx.id) }
                    )
                }
            }
        }
    }
}

/**
 * Single Transaction List Item Card.
 */
@Composable
fun TransactionListItemCard(
    transaction: TransactionItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Icon
                Surface(
                    shape = CircleShape,
                    color = transaction.categoryColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = transaction.categoryIcon,
                            contentDescription = transaction.categoryName,
                            tint = transaction.categoryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${transaction.categoryName} • ${transaction.timeFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = transaction.amountFormatted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isExpense) SemanticRed else SemanticGreen,
                maxLines = 1,
                softWrap = false
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
            transactionGroups = listOf(
                TransactionGroupUi(
                    dateHeader = "Hôm nay, 15 Th08",
                    dailyNetFormatted = "-120.000 ₫",
                    transactions = listOf(
                        TransactionItemUi(
                            id = 1L,
                            title = "Ăn trưa bún bò",
                            categoryName = "Ăn uống",
                            categoryIcon = Icons.Default.ReceiptLong,
                            categoryColor = Color(0xFFFFA000),
                            amountFormatted = "-50.000 ₫",
                            isExpense = true,
                            timeFormatted = "12:30",
                            date = LocalDate.now()
                        )
                    )
                )
            ),
            totalCount = 1,
            totalIncomeFormatted = "+0 ₫",
            totalExpenseFormatted = "-50.000 ₫",
            isLoading = false,
            onTransactionClick = {},
            onAddTransactionClick = {}
        )
    }
}
