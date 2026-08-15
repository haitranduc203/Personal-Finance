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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.fintrack.app.ui.components.EmptyStateView
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticGreen
import com.fintrack.app.ui.theme.SemanticRed

/**
 * Grouped transactions item for list display.
 */
data class DateGroupedTransactions(
    val dateHeader: String,
    val totalExpenseForDay: String,
    val items: List<TransactionListItem>
)

data class TransactionListItem(
    val id: Long,
    val title: String,
    val categoryName: String,
    val categoryIcon: ImageVector,
    val categoryColor: Color,
    val amountFormatted: String,
    val isExpense: Boolean,
    val timeFormatted: String
)

/**
 * Stateful entry composable for Transactions Screen.
 */
@Composable
fun TransactionsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val sampleGroupedTransactions = listOf(
        DateGroupedTransactions(
            dateHeader = "Hôm nay, 15 Th08",
            totalExpenseForDay = "-120.000 ₫",
            items = listOf(
                TransactionListItem(
                    id = 1L,
                    title = "Cà phê sáng & Bánh mì",
                    categoryName = "Ăn uống",
                    categoryIcon = Icons.Default.Fastfood,
                    categoryColor = Color(0xFFFFA000),
                    amountFormatted = "-50.000 ₫",
                    isExpense = true,
                    timeFormatted = "08:15"
                ),
                TransactionListItem(
                    id = 2L,
                    title = "Đổ xăng xe máy",
                    categoryName = "Đi lại",
                    categoryIcon = Icons.Default.LocalGasStation,
                    categoryColor = Color(0xFF0288D1),
                    amountFormatted = "-70.000 ₫",
                    isExpense = true,
                    timeFormatted = "11:30"
                )
            )
        ),
        DateGroupedTransactions(
            dateHeader = "Hôm qua, 14 Th08",
            totalExpenseForDay = "+18.000.000 ₫",
            items = listOf(
                TransactionListItem(
                    id = 3L,
                    title = "Nhận lương tháng 8",
                    categoryName = "Tiền lương",
                    categoryIcon = Icons.Default.Paid,
                    categoryColor = SemanticGreen,
                    amountFormatted = "+18.000.000 ₫",
                    isExpense = false,
                    timeFormatted = "09:00"
                )
            )
        ),
        DateGroupedTransactions(
            dateHeader = "12 Th08, 2026",
            totalExpenseForDay = "-420.000 ₫",
            items = listOf(
                TransactionListItem(
                    id = 4L,
                    title = "Mua nhu yếu phẩm Co.opmart",
                    categoryName = "Mua sắm",
                    categoryIcon = Icons.Default.ShoppingCart,
                    categoryColor = Color(0xFF7B1FA2),
                    amountFormatted = "-420.000 ₫",
                    isExpense = true,
                    timeFormatted = "19:45"
                ),
                TransactionListItem(
                    id = 5L,
                    title = "Thưởng dự án Freelance",
                    categoryName = "Thu nhập phụ",
                    categoryIcon = Icons.Default.Work,
                    categoryColor = Color(0xFF00796B),
                    amountFormatted = "+2.500.000 ₫",
                    isExpense = false,
                    timeFormatted = "14:20"
                )
            )
        )
    )

    TransactionsScreenContent(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedFilterIndex = selectedFilterIndex,
        onFilterSelected = { selectedFilterIndex = it },
        groupedTransactions = sampleGroupedTransactions,
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
    selectedFilterIndex: Int,
    onFilterSelected: (Int) -> Unit,
    groupedTransactions: List<DateGroupedTransactions>,
    onTransactionClick: (Long) -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("Tất cả", "Khoản chi", "Khoản thu", "Tháng này")

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
                items(filters.size) { index ->
                    FilterChip(
                        selected = selectedFilterIndex == index,
                        onClick = { onFilterSelected(index) },
                        label = { Text(filters[index]) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
        }

        // Summary Banner of filtered period
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng 5 giao dịch",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Thu: +20.500.000 ₫",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SemanticGreen
                    )
                    Text(
                        text = "Chi: -540.000 ₫",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SemanticRed
                    )
                }
            }
        }

        // Transaction Groups List
        if (groupedTransactions.isEmpty()) {
            EmptyStateView(
                title = "Không tìm thấy giao dịch nào",
                description = "Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc ngày.",
                actionButtonText = "Thêm giao dịch mới",
                onActionClick = onAddTransactionClick,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedTransactions.forEach { group ->
                    item(key = group.dateHeader) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Date Group Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.dateHeader,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = group.totalExpenseForDay,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Items under this group
                            group.items.forEach { item ->
                                TransactionRowItem(
                                    item = item,
                                    onClick = { onTransactionClick(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single Transaction Row Item Card.
 */
@Composable
fun TransactionRowItem(
    item: TransactionListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(item.categoryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.categoryIcon,
                    contentDescription = item.categoryName,
                    tint = item.categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.categoryName} • ${item.timeFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = item.amountFormatted,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (item.isExpense) SemanticRed else SemanticGreen
            )
        }
    }
}

// ----------------------------------------------------
// Compose Previews
// ----------------------------------------------------

@PreviewLightDark
@Composable
private fun TransactionsScreenPopulatedPreview() {
    FinTrackTheme {
        TransactionsScreen(
            onNavigateToDetail = {},
            onNavigateToAddTransaction = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun TransactionsScreenEmptyPreview() {
    FinTrackTheme {
        TransactionsScreenContent(
            searchQuery = "Tìm kiếm...",
            onSearchQueryChange = {},
            selectedFilterIndex = 0,
            onFilterSelected = {},
            groupedTransactions = emptyList(),
            onTransactionClick = {},
            onAddTransactionClick = {}
        )
    }
}
