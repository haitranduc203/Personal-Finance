package com.fintrack.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.app.R
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.ui.components.EmptyStateView
import com.fintrack.app.ui.theme.FinTrackTheme
import com.fintrack.app.ui.theme.SemanticGreen
import com.fintrack.app.ui.theme.SemanticRed
import com.fintrack.app.ui.util.CategoryIconHelper
import com.fintrack.app.ui.util.CurrencyFormatter
import com.fintrack.app.ui.util.toLocalDateTime
import com.fintrack.app.ui.viewmodel.AppViewModelProvider
import java.time.format.DateTimeFormatter

/**
 * Model for rendering a transaction item on the Home Screen.
 */
data class HomeTransactionUi(
    val id: Long,
    val title: String,
    val categoryName: String,
    val categoryIcon: ImageVector,
    val categoryColor: Color,
    val amountFormatted: String,
    val isExpense: Boolean,
    val dateFormatted: String
)

/**
 * Stateful entry composable for Home Screen.
 */
@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeFormatter = DateTimeFormatter.ofPattern("dd/MM, HH:mm")

    val transactionsUi = uiState.recentTransactions.map { txWithCat ->
        val isExpense = txWithCat.transaction.type == TransactionType.EXPENSE
        val amountStr = CurrencyFormatter.format(
            amount = txWithCat.transaction.amount,
            currency = uiState.currency,
            withSign = true,
            isExpense = isExpense,
            isIncome = !isExpense
        )
        val dt = txWithCat.transaction.transactionDate.toLocalDateTime()

        HomeTransactionUi(
            id = txWithCat.transaction.id,
            title = txWithCat.transaction.note?.ifBlank { null } ?: txWithCat.category.name,
            categoryName = txWithCat.category.name,
            categoryIcon = CategoryIconHelper.getIconByName(txWithCat.category.iconKey),
            categoryColor = CategoryIconHelper.parseColor(txWithCat.category.colorKey),
            amountFormatted = amountStr,
            isExpense = isExpense,
            dateFormatted = dt.format(timeFormatter)
        )
    }

    val balanceFormatted = CurrencyFormatter.format(
        amount = uiState.balance,
        currency = uiState.currency,
        withSign = false
    )
    val incomeFormatted = CurrencyFormatter.format(
        amount = uiState.totalIncome,
        currency = uiState.currency,
        withSign = true,
        isIncome = true
    )
    val expenseFormatted = CurrencyFormatter.format(
        amount = uiState.totalExpense,
        currency = uiState.currency,
        withSign = true,
        isExpense = true
    )

    HomeScreenContent(
        balanceFormatted = balanceFormatted,
        incomeFormatted = incomeFormatted,
        expenseFormatted = expenseFormatted,
        selectedMonth = uiState.selectedMonth,
        insight = uiState.insight,
        recentTransactions = transactionsUi,
        onSeeAllTransactionsClick = onNavigateToTransactions,
        onTransactionClick = onNavigateToTransactionDetail,
        onAddTransactionClick = onNavigateToAddTransaction,
        modifier = modifier
    )
}

/**
 * Stateless pure UI component for Home Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    balanceFormatted: String,
    incomeFormatted: String,
    expenseFormatted: String,
    selectedMonth: String,
    insight: FinancialInsightUi,
    recentTransactions: List<HomeTransactionUi>,
    onSeeAllTransactionsClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onAddTransactionClick: () -> Unit,
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
                .padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.home_dashboard_title),
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
                            contentDescription = stringResource(R.string.home_balance_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = selectedMonth,
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
                        contentDescription = stringResource(R.string.notif_title),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Balance Card
            item {
                HeroBalanceCard(
                    balance = balanceFormatted,
                    income = incomeFormatted,
                    expense = expenseFormatted
                )
            }

            // 2. Trend Preview Card
            item {
                TrendPreviewCard(insight = insight)
            }

            // 3. Recent Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_recent_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onSeeAllTransactionsClick) {
                        Text(
                            text = stringResource(R.string.home_see_all),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 4. Recent Transactions List
            if (recentTransactions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = stringResource(R.string.home_empty_title),
                        description = stringResource(R.string.home_empty_desc),
                        actionButtonText = stringResource(R.string.home_add_transaction),
                        onActionClick = onAddTransactionClick
                    )
                }
            } else {
                items(recentTransactions, key = { it.id }) { transaction ->
                    HomeTransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction.id) }
                    )
                }
            }
        }
    }
}

/**
 * Hero Balance Card displaying available balance and income/expense breakdown.
 */
@Composable
fun HeroBalanceCard(
    balance: String,
    income: String,
    expense: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.home_balance_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = balance,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Income / Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SemanticGreen.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SemanticGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = stringResource(R.string.home_income),
                                tint = SemanticGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.home_income),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = income,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SemanticGreen,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Expense Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SemanticRed.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SemanticRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = stringResource(R.string.home_expense),
                                tint = SemanticRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.home_expense),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = expense,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SemanticRed,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dynamic Trend & Financial Insight Card on Home Dashboard.
 */
@Composable
fun TrendPreviewCard(
    insight: FinancialInsightUi,
    modifier: Modifier = Modifier
) {
    val (icon, tint, bgColor) = when (insight.iconType) {
        InsightIconType.SAVING_UP -> Triple(
            Icons.AutoMirrored.Filled.TrendingUp,
            SemanticGreen,
            SemanticGreen.copy(alpha = 0.12f)
        )
        InsightIconType.OVERSPENT -> Triple(
            Icons.AutoMirrored.Filled.TrendingDown,
            SemanticRed,
            SemanticRed.copy(alpha = 0.12f)
        )
        InsightIconType.NO_EXPENSE -> Triple(
            Icons.Default.CheckCircle,
            SemanticGreen,
            SemanticGreen.copy(alpha = 0.12f)
        )
        InsightIconType.FIRST_STEPS -> Triple(
            Icons.Default.Info,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Single Transaction Item Card for Home list.
 */
@Composable
fun HomeTransactionItem(
    transaction: HomeTransactionUi,
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
            // Category Icon with subtle tinted background
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(transaction.categoryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.categoryIcon,
                    contentDescription = transaction.categoryName,
                    tint = transaction.categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.categoryName} • ${transaction.dateFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Amount
            Text(
                text = transaction.amountFormatted,
                style = MaterialTheme.typography.bodyLarge,
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
private fun HomeScreenEmptyPreview() {
    FinTrackTheme {
        HomeScreenContent(
            balanceFormatted = "0 ₫",
            incomeFormatted = "0 ₫",
            expenseFormatted = "0 ₫",
            selectedMonth = "Tháng 8, 2026",
            insight = FinancialInsightUi(
                title = "Sẵn sàng quản lý tài chính",
                description = "Ghi chép giao dịch đầu tiên để theo dõi dòng tiền hiệu quả"
            ),
            recentTransactions = emptyList(),
            onSeeAllTransactionsClick = {},
            onTransactionClick = {},
            onAddTransactionClick = {}
        )
    }
}
