package com.fintrack.app.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.local.preferences.CurrencyConfig
import com.fintrack.app.data.repository.PreferencesRepository
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.ui.util.CurrencyFormatter
import com.fintrack.app.ui.util.toLocalDate
import com.fintrack.app.ui.util.toLocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
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
 * Clean domain model for rendering a transaction item in list (no Compose UI dependencies).
 */
data class TransactionItemUi(
    val id: Long,
    val title: String,
    val categoryName: String,
    val categoryIconKey: String,
    val categoryColorKey: String,
    val amountFormatted: String,
    val rawAmount: Long,
    val isExpense: Boolean,
    val timeFormatted: String,
    val date: LocalDate
)

/**
 * Domain model representing grouped transactions by date.
 */
data class TransactionGroupUi(
    val dateHeader: String,
    val dailyNetFormatted: String,
    val transactions: List<TransactionItemUi>
)

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
    val currency: CurrencyConfig = CurrencyConfig.VND,
    val isLoading: Boolean = false
)

/**
 * ViewModel managing transactions list, reactive search, filtering, and summary metrics.
 */
class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactionRepository.observeTransactions(),
        _searchQuery,
        _selectedFilter,
        preferencesRepository.userPreferencesFlow
    ) { allTx, query, filter, prefs ->
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val currentYearMonth = YearMonth.from(now)
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
            val dt = item.transaction.transactionDate.toLocalDateTime()

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

        // Build grouped UI models
        val groups = filtered
            .map { txWithCat ->
                val isExpense = txWithCat.transaction.type == TransactionType.EXPENSE
                val amountStr = CurrencyFormatter.format(
                    amount = txWithCat.transaction.amount,
                    currency = prefs.currency,
                    withSign = true,
                    isExpense = isExpense,
                    isIncome = !isExpense
                )
                val dt = txWithCat.transaction.transactionDate.toLocalDateTime()

                TransactionItemUi(
                    id = txWithCat.transaction.id,
                    title = txWithCat.transaction.note?.ifBlank { null } ?: txWithCat.category.name,
                    categoryName = txWithCat.category.name,
                    categoryIconKey = txWithCat.category.iconKey,
                    categoryColorKey = txWithCat.category.colorKey,
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
                var netDailyAmount = 0L
                items.forEach { item ->
                    if (item.isExpense) netDailyAmount -= item.rawAmount else netDailyAmount += item.rawAmount
                }
                val netStr = CurrencyFormatter.format(
                    amount = netDailyAmount,
                    currency = prefs.currency,
                    withSign = true
                )

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
            currency = prefs.currency,
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
