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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * Filter mode for transaction types (Thu / Chi).
 */
enum class TransactionFilter(val title: String) {
    ALL("Tất cả"),
    EXPENSE("Khoản chi"),
    INCOME("Khoản thu")
}

/**
 * Period filter mode for Period Navigator.
 */
enum class PeriodFilterMode(val title: String) {
    ALL_TIME("Toàn bộ thời gian"),
    DAY("Theo ngày"),
    WEEK("Theo tuần"),
    MONTH("Theo tháng"),
    YEAR("Theo năm"),
    CUSTOM_RANGE("Khoảng ngày tùy chọn")
}

/**
 * State holding active period filter parameters.
 */
data class PeriodFilterState(
    val mode: PeriodFilterMode = PeriodFilterMode.MONTH,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val selectedYear: Int = LocalDate.now().year,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null
)

/**
 * Clean domain model for rendering a transaction item in list.
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
    val periodState: PeriodFilterState = PeriodFilterState(),
    val currentPeriodTitle: String = "",
    val canNavigatePeriod: Boolean = true,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val totalCount: Int = 0,
    val currency: CurrencyConfig = CurrencyConfig.VND,
    val isLoading: Boolean = false
)

/**
 * ViewModel managing transactions list with Period Navigator (Prev/Next buttons), reactive search, and summary.
 */
class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    private val _periodState = MutableStateFlow(PeriodFilterState())

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactionRepository.observeTransactions(),
        _searchQuery,
        _selectedFilter,
        _periodState,
        preferencesRepository.userPreferencesFlow
    ) { allTx, query, typeFilter, periodState, prefs ->
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // 1. Calculate Period Title & Date Boundaries
        val periodTitle: String
        val canNavigate: Boolean

        when (periodState.mode) {
            PeriodFilterMode.ALL_TIME -> {
                periodTitle = "Toàn bộ thời gian"
                canNavigate = false
            }
            PeriodFilterMode.DAY -> {
                val d = periodState.selectedDate
                periodTitle = if (d == today) {
                    "Hôm nay (${d.format(DateTimeFormatter.ofPattern("dd/MM"))})"
                } else {
                    d.format(dateFormatter)
                }
                canNavigate = true
            }
            PeriodFilterMode.WEEK -> {
                val start = periodState.selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val end = periodState.selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                periodTitle = "${start.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                canNavigate = true
            }
            PeriodFilterMode.MONTH -> {
                val ym = periodState.selectedYearMonth
                periodTitle = "Tháng ${ym.monthValue}, ${ym.year}"
                canNavigate = true
            }
            PeriodFilterMode.YEAR -> {
                periodTitle = "Năm ${periodState.selectedYear}"
                canNavigate = true
            }
            PeriodFilterMode.CUSTOM_RANGE -> {
                val s = periodState.customStartDate ?: today
                val e = periodState.customEndDate ?: today
                periodTitle = "${s.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${e.format(dateFormatter)}"
                canNavigate = false
            }
        }

        // 2. Filter Transactions
        val filtered = allTx.filter { item ->
            val dt = item.transaction.transactionDate.toLocalDateTime()
            val txDate = dt.toLocalDate()

            // A. Type Filter
            val matchesType = when (typeFilter) {
                TransactionFilter.ALL -> true
                TransactionFilter.EXPENSE -> item.transaction.type == TransactionType.EXPENSE
                TransactionFilter.INCOME -> item.transaction.type == TransactionType.INCOME
            }

            // B. Period Filter
            val matchesPeriod = when (periodState.mode) {
                PeriodFilterMode.ALL_TIME -> true
                PeriodFilterMode.DAY -> txDate == periodState.selectedDate
                PeriodFilterMode.WEEK -> {
                    val start = periodState.selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val end = periodState.selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    !txDate.isBefore(start) && !txDate.isAfter(end)
                }
                PeriodFilterMode.MONTH -> YearMonth.from(dt) == periodState.selectedYearMonth
                PeriodFilterMode.YEAR -> dt.year == periodState.selectedYear
                PeriodFilterMode.CUSTOM_RANGE -> {
                    val s = periodState.customStartDate
                    val e = periodState.customEndDate
                    if (s != null && e != null) {
                        !txDate.isBefore(s) && !txDate.isAfter(e)
                    } else true
                }
            }

            // C. Search Query
            val cleanQuery = query.trim().lowercase()
            val matchesQuery = if (cleanQuery.isEmpty()) {
                true
            } else {
                val catName = item.category.name.lowercase()
                val note = item.transaction.note?.lowercase() ?: ""
                catName.contains(cleanQuery) || note.contains(cleanQuery)
            }

            matchesType && matchesPeriod && matchesQuery
        }

        // 3. Summary Calculations
        var incomeSum = 0L
        var expenseSum = 0L
        filtered.forEach { item ->
            if (item.transaction.type == TransactionType.INCOME) {
                incomeSum += item.transaction.amount
            } else {
                expenseSum += item.transaction.amount
            }
        }

        // 4. Group by Date
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
            selectedFilter = typeFilter,
            periodState = periodState,
            currentPeriodTitle = periodTitle,
            canNavigatePeriod = canNavigate,
            totalIncome = incomeSum,
            totalExpense = expenseSum,
            totalCount = filtered.size,
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

    /**
     * Navigate backward in the current period (previous month, previous week, previous day, or previous year).
     */
    fun onPreviousPeriod() {
        val current = _periodState.value
        _periodState.value = when (current.mode) {
            PeriodFilterMode.MONTH -> current.copy(selectedYearMonth = current.selectedYearMonth.minusMonths(1))
            PeriodFilterMode.DAY -> current.copy(selectedDate = current.selectedDate.minusDays(1))
            PeriodFilterMode.WEEK -> current.copy(selectedDate = current.selectedDate.minusWeeks(1))
            PeriodFilterMode.YEAR -> current.copy(selectedYear = current.selectedYear - 1)
            else -> current
        }
    }

    /**
     * Navigate forward in the current period (next month, next week, next day, or next year).
     */
    fun onNextPeriod() {
        val current = _periodState.value
        _periodState.value = when (current.mode) {
            PeriodFilterMode.MONTH -> current.copy(selectedYearMonth = current.selectedYearMonth.plusMonths(1))
            PeriodFilterMode.DAY -> current.copy(selectedDate = current.selectedDate.plusDays(1))
            PeriodFilterMode.WEEK -> current.copy(selectedDate = current.selectedDate.plusWeeks(1))
            PeriodFilterMode.YEAR -> current.copy(selectedYear = current.selectedYear + 1)
            else -> current
        }
    }

    /**
     * Switch to a specific period mode (e.g. Month, Week, Day, Year, All time).
     */
    fun onSelectPeriodMode(mode: PeriodFilterMode) {
        _periodState.value = _periodState.value.copy(mode = mode)
    }

    fun onSelectThisMonth() {
        _periodState.value = _periodState.value.copy(
            mode = PeriodFilterMode.MONTH,
            selectedYearMonth = YearMonth.now()
        )
    }

    fun onSelectToday() {
        _periodState.value = _periodState.value.copy(
            mode = PeriodFilterMode.DAY,
            selectedDate = LocalDate.now()
        )
    }

    fun onSelectThisWeek() {
        _periodState.value = _periodState.value.copy(
            mode = PeriodFilterMode.WEEK,
            selectedDate = LocalDate.now()
        )
    }

    fun onSelectThisYear() {
        _periodState.value = _periodState.value.copy(
            mode = PeriodFilterMode.YEAR,
            selectedYear = LocalDate.now().year
        )
    }

    fun onSelectAllTime() {
        _periodState.value = _periodState.value.copy(mode = PeriodFilterMode.ALL_TIME)
    }

    fun onSpecificDateSelected(date: LocalDate) {
        _periodState.value = _periodState.value.copy(
            mode = PeriodFilterMode.DAY,
            selectedDate = date
        )
    }

    fun onDateRangeSelected(startDate: LocalDate, endDate: LocalDate) {
        val (start, end) = if (startDate.isAfter(endDate)) Pair(endDate, startDate) else Pair(startDate, endDate)
        _periodState.value = _periodState.value.copy(
            mode = PeriodFilterMode.CUSTOM_RANGE,
            customStartDate = start,
            customEndDate = end
        )
    }

    fun resetToDefaultMonth() {
        _searchQuery.value = ""
        _selectedFilter.value = TransactionFilter.ALL
        _periodState.value = PeriodFilterState(
            mode = PeriodFilterMode.MONTH,
            selectedYearMonth = YearMonth.now()
        )
    }
}
