package com.fintrack.app.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.ui.util.CategoryIconHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Filter mode for transactions list.
 */
enum class TransactionFilter(val title: String) {
    ALL("Tất cả"),
    EXPENSE("Khoản chi"),
    INCOME("Khoản thu"),
    THIS_MONTH("Tháng này")
}

/**
 * UI State for Transactions List Screen.
 */
data class TransactionsUiState(
    val allTransactions: List<TransactionWithCategory> = emptyList(),
    val filteredTransactions: List<TransactionWithCategory> = emptyList(),
    val transactionGroups: List<TransactionGroupUi> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val totalCount: Int = 0,
    val currentMonthLabel: String = "",
    val isLoading: Boolean = false
)

/**
 * ViewModel managing transactions list, reactive search, filtering, and summary metrics.
 */
class TransactionsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactionRepository.observeTransactions(),
        _searchQuery,
        _selectedFilter
    ) { allTx, query, filter ->
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val currentYearMonth = YearMonth.from(now)
        val decimalFormat = DecimalFormat("#,###")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        var incomeSum = 0L
        var expenseSum = 0L
        allTx.forEach { item ->
            if (item.transaction.type == TransactionType.INCOME) {
                incomeSum += item.transaction.amount
            } else {
                expenseSum += item.transaction.amount
            }
        }

        // Apply filters
        val filtered = allTx.filter { item ->
            val dt = Instant.ofEpochMilli(item.transaction.transactionDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val matchesFilter = when (filter) {
                TransactionFilter.ALL -> true
                TransactionFilter.EXPENSE -> item.transaction.type == TransactionType.EXPENSE
                TransactionFilter.INCOME -> item.transaction.type == TransactionType.INCOME
                TransactionFilter.THIS_MONTH -> YearMonth.from(dt) == currentYearMonth
            }

            val cleanQuery = query.trim().lowercase()
            val matchesQuery = if (cleanQuery.isEmpty()) {
                true
            } else {
                val catName = item.category.name.lowercase()
                val note = item.transaction.note?.lowercase() ?: ""
                catName.contains(cleanQuery) || note.contains(cleanQuery)
            }

            matchesFilter && matchesQuery
        }

        // Build grouped UI models — done here once, not on every recomposition
        val groups = filtered
            .map { txWithCat ->
                val isExpense = txWithCat.transaction.type == TransactionType.EXPENSE
                val prefix = if (isExpense) "-" else "+"
                val amountStr = "$prefix${decimalFormat.format(txWithCat.transaction.amount)} ₫"
                val dt = Instant.ofEpochMilli(txWithCat.transaction.transactionDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

                TransactionItemUi(
                    id = txWithCat.transaction.id,
                    title = txWithCat.transaction.note ?: txWithCat.category.name,
                    categoryName = txWithCat.category.name,
                    categoryIcon = CategoryIconHelper.getIconByName(txWithCat.category.iconKey),
                    categoryColor = CategoryIconHelper.parseColor(txWithCat.category.colorKey),
                    amountFormatted = amountStr,
                    rawAmount = txWithCat.transaction.amount,
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
                // Tính netDaily từ số nguyên thuần — không parse chuỗi đã format
                var netDailyAmount = 0L
                items.forEach { item ->
                    if (item.isExpense) netDailyAmount -= item.rawAmount else netDailyAmount += item.rawAmount
                }
                val netPrefix = if (netDailyAmount >= 0) "+" else "-"
                val netStr = "$netPrefix${decimalFormat.format(Math.abs(netDailyAmount.toDouble()))} ₫"

                TransactionGroupUi(
                    dateHeader = headerTitle,
                    dailyNetFormatted = netStr,
                    transactions = items
                )
            }

        TransactionsUiState(
            allTransactions = allTx,
            filteredTransactions = filtered,
            transactionGroups = groups,
            searchQuery = query,
            selectedFilter = filter,
            totalIncome = incomeSum,
            totalExpense = expenseSum,
            totalCount = filtered.size,
            currentMonthLabel = "Tháng ${now.monthValue}, ${now.year}",
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: TransactionFilter) {
        _selectedFilter.value = filter
    }

    fun onFilterIndexSelected(index: Int) {
        val filters = TransactionFilter.entries
        if (index in filters.indices) {
            _selectedFilter.value = filters[index]
        }
    }
}
