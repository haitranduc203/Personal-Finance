package com.fintrack.app.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

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
    val searchQuery: String = "",
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val totalCount: Int = 0,
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
        val currentYearMonth = YearMonth.from(now)

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

        TransactionsUiState(
            allTransactions = allTx,
            filteredTransactions = filtered,
            searchQuery = query,
            selectedFilter = filter,
            totalIncome = incomeSum,
            totalExpense = expenseSum,
            totalCount = filtered.size,
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
