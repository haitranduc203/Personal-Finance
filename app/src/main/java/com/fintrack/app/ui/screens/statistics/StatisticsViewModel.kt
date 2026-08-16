package com.fintrack.app.ui.screens.statistics

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.model.CategoryExpense
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.ui.util.CategoryIconHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Period options for statistics filtering.
 */
enum class StatisticsPeriod(val title: String) {
    WEEK("Tuần"),
    MONTH("Tháng"),
    YEAR("Năm")
}

/**
 * UI model representing expense breakdown for a single category.
 */
data class CategoryStatItem(
    val categoryId: Long,
    val name: String,
    val iconKey: String,
    val totalAmount: Long,
    val totalAmountFormatted: String,
    val percentage: Float, // 0.0 to 1.0
    val color: Color
)

/**
 * UI model representing a grouped pair of Income & Expense bars in the bar chart.
 */
data class BarChartGroup(
    val label: String,
    val incomeAmount: Long,
    val expenseAmount: Long,
    val incomeFraction: Float, // 0.0f to 1.0f
    val expenseFraction: Float // 0.0f to 1.0f
)

/**
 * UI State for Statistics Screen.
 */
data class StatisticsUiState(
    val selectedPeriodIndex: Int = 1, // 0: Tuần, 1: Tháng, 2: Năm
    val periodTitle: String = "",
    val totalIncome: Long = 0L,
    val totalIncomeFormatted: String = "+0 ₫",
    val totalExpense: Long = 0L,
    val totalExpenseFormatted: String = "-0 ₫",
    val dailyAverage: Long = 0L,
    val dailyAverageFormatted: String = "-0 ₫",
    val categoryStats: List<CategoryStatItem> = emptyList(),
    val barChartGroups: List<BarChartGroup> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel managing analytics, calculations, and chart representations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _selectedPeriodIndex = MutableStateFlow(1) // Default: Tháng (Month)

    val uiState: StateFlow<StatisticsUiState> = _selectedPeriodIndex
        .flatMapLatest { periodIndex ->
            val now = LocalDateTime.now()
            val (startDate, endDate, periodTitle, daysInPeriod, daysElapsed) = calculatePeriodBounds(periodIndex, now)

            transactionRepository.observeTransactionsByPeriod(startDate, endDate)
                .map { transactions ->
                    calculateStatisticsUiState(
                        periodIndex = periodIndex,
                        periodTitle = periodTitle,
                        daysInPeriod = daysInPeriod,
                        daysElapsed = daysElapsed,
                        transactions = transactions
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsUiState(isLoading = true)
        )

    fun selectPeriod(index: Int) {
        _selectedPeriodIndex.value = index
    }

    private data class PeriodBounds(
        val startDateMillis: Long,
        val endDateMillis: Long,
        val title: String,
        val daysInPeriod: Int,
        val daysElapsed: Int  // Số ngày thực sự đã trôi qua trong kỳ (để tính daily average chính xác)
    )

    private fun calculatePeriodBounds(periodIndex: Int, now: LocalDateTime): PeriodBounds {
        val zone = ZoneId.systemDefault()
        val today = now.toLocalDate()
        return when (periodIndex) {
            0 -> {
                // Tuần này (Thứ 2 đến Chủ nhật)
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val start = monday.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = sunday.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
                val title = "Tuần ${monday.dayOfMonth}/${monday.monthValue} - ${sunday.dayOfMonth}/${sunday.monthValue}"
                // Số ngày đã trôi qua: từ Thứ 2 đến hôm nay (tối đa 7)
                val elapsed = (java.time.temporal.ChronoUnit.DAYS.between(monday, minOf(today, sunday)).toInt() + 1).coerceIn(1, 7)
                PeriodBounds(start, end, title, 7, elapsed)
            }
            1 -> {
                // Tháng này
                val firstDay = today.with(TemporalAdjusters.firstDayOfMonth())
                val lastDay = today.with(TemporalAdjusters.lastDayOfMonth())
                val start = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = lastDay.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
                val title = "Tháng ${now.monthValue}, ${now.year}"
                // Số ngày đã trôi qua: ngày hiện tại trong tháng
                PeriodBounds(start, end, title, lastDay.dayOfMonth, today.dayOfMonth)
            }
            else -> {
                // Năm này
                val firstDay = today.with(TemporalAdjusters.firstDayOfYear())
                val lastDay = today.with(TemporalAdjusters.lastDayOfYear())
                val start = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = lastDay.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
                val title = "Năm ${now.year}"
                val totalDays = if (today.isLeapYear) 366 else 365
                // Số ngày đã trôi qua: ngày thứ mấy trong năm
                PeriodBounds(start, end, title, totalDays, today.dayOfYear)
            }
        }
    }

    private fun calculateStatisticsUiState(
        periodIndex: Int,
        periodTitle: String,
        daysInPeriod: Int,
        daysElapsed: Int,
        transactions: List<TransactionWithCategory>
    ): StatisticsUiState {
        var totalIncome = 0L
        var totalExpense = 0L
        val expenseCategoryMap = mutableMapOf<Long, CategoryExpenseAccumulator>()

        transactions.forEach { item ->
            val tx = item.transaction
            val cat = item.category
            if (tx.type == TransactionType.INCOME) {
                totalIncome += tx.amount
            } else {
                totalExpense += tx.amount
                val current = expenseCategoryMap.getOrPut(cat.id) {
                    CategoryExpenseAccumulator(
                        categoryId = cat.id,
                        categoryName = cat.name,
                        iconKey = cat.iconKey,
                        colorKey = cat.colorKey,
                        totalAmount = 0L
                    )
                }
                current.totalAmount += tx.amount
            }
        }

        // Chia theo số ngày đã thực sự trôi qua (daysElapsed), không phải toàn bộ kỳ (daysInPeriod)
        // Ví dụ: ngày 16/8 → chia 16, không chia 31
        val dailyAvg = if (daysElapsed > 0) totalExpense / daysElapsed else 0L

        // Category stats sorted by totalAmount DESC
        val categoryStats = expenseCategoryMap.values
            .sortedByDescending { it.totalAmount }
            .map { acc ->
                val percentage = if (totalExpense > 0) {
                    acc.totalAmount.toFloat() / totalExpense.toFloat()
                } else 0f
                CategoryStatItem(
                    categoryId = acc.categoryId,
                    name = acc.categoryName,
                    iconKey = acc.iconKey,
                    totalAmount = acc.totalAmount,
                    totalAmountFormatted = formatCurrency(acc.totalAmount, isExpense = false),
                    percentage = percentage,
                    color = CategoryIconHelper.parseColor(acc.colorKey)
                )
            }

        // Bar Chart groups calculation
        val barChartGroups = generateBarChartGroups(periodIndex, transactions)

        return StatisticsUiState(
            selectedPeriodIndex = periodIndex,
            periodTitle = periodTitle,
            totalIncome = totalIncome,
            totalIncomeFormatted = formatCurrency(totalIncome, isIncome = true),
            totalExpense = totalExpense,
            totalExpenseFormatted = formatCurrency(totalExpense, isExpense = true),
            dailyAverage = dailyAvg,
            dailyAverageFormatted = formatCurrency(dailyAvg, isExpense = true),
            categoryStats = categoryStats,
            barChartGroups = barChartGroups,
            isLoading = false
        )
    }

    private data class CategoryExpenseAccumulator(
        val categoryId: Long,
        val categoryName: String,
        val iconKey: String,
        val colorKey: String,
        var totalAmount: Long
    )

    private fun generateBarChartGroups(
        periodIndex: Int,
        transactions: List<TransactionWithCategory>
    ): List<BarChartGroup> {
        val zone = ZoneId.systemDefault()
        val groups: List<Pair<String, Pair<Long, Long>>> = when (periodIndex) {
            0 -> {
                // 7 days of week: Thứ 2 -> Chủ nhật
                val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                val dayMap = dayLabels.associateWith { Pair(0L, 0L) }.toMutableMap()

                transactions.forEach { item ->
                    val dt = Instant.ofEpochMilli(item.transaction.transactionDate).atZone(zone).toLocalDateTime()
                    val dayOfWeekIndex = when (dt.dayOfWeek) {
                        DayOfWeek.MONDAY -> 0
                        DayOfWeek.TUESDAY -> 1
                        DayOfWeek.WEDNESDAY -> 2
                        DayOfWeek.THURSDAY -> 3
                        DayOfWeek.FRIDAY -> 4
                        DayOfWeek.SATURDAY -> 5
                        DayOfWeek.SUNDAY -> 6
                    }
                    val label = dayLabels[dayOfWeekIndex]
                    val current = dayMap[label] ?: Pair(0L, 0L)
                    if (item.transaction.type == TransactionType.INCOME) {
                        dayMap[label] = Pair(current.first + item.transaction.amount, current.second)
                    } else {
                        dayMap[label] = Pair(current.first, current.second + item.transaction.amount)
                    }
                }
                dayLabels.map { label -> label to (dayMap[label] ?: Pair(0L, 0L)) }
            }
            1 -> {
                // 4 weeks in month: Tuần 1 (1-7), Tuần 2 (8-14), Tuần 3 (15-21), Tuần 4 (22-end)
                val weekLabels = listOf("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4")
                val weekMap = weekLabels.associateWith { Pair(0L, 0L) }.toMutableMap()

                transactions.forEach { item ->
                    val dt = Instant.ofEpochMilli(item.transaction.transactionDate).atZone(zone).toLocalDateTime()
                    val day = dt.dayOfMonth
                    val label = when {
                        day <= 7 -> "Tuần 1"
                        day <= 14 -> "Tuần 2"
                        day <= 21 -> "Tuần 3"
                        else -> "Tuần 4"
                    }
                    val current = weekMap[label] ?: Pair(0L, 0L)
                    if (item.transaction.type == TransactionType.INCOME) {
                        weekMap[label] = Pair(current.first + item.transaction.amount, current.second)
                    } else {
                        weekMap[label] = Pair(current.first, current.second + item.transaction.amount)
                    }
                }
                weekLabels.map { label -> label to (weekMap[label] ?: Pair(0L, 0L)) }
            }
            else -> {
                // 4 quarters in year: Quý 1, Quý 2, Quý 3, Quý 4
                val quarterLabels = listOf("Quý 1", "Quý 2", "Quý 3", "Quý 4")
                val quarterMap = quarterLabels.associateWith { Pair(0L, 0L) }.toMutableMap()

                transactions.forEach { item ->
                    val dt = Instant.ofEpochMilli(item.transaction.transactionDate).atZone(zone).toLocalDateTime()
                    val month = dt.monthValue
                    val label = when {
                        month <= 3 -> "Quý 1"
                        month <= 6 -> "Quý 2"
                        month <= 9 -> "Quý 3"
                        else -> "Quý 4"
                    }
                    val current = quarterMap[label] ?: Pair(0L, 0L)
                    if (item.transaction.type == TransactionType.INCOME) {
                        quarterMap[label] = Pair(current.first + item.transaction.amount, current.second)
                    } else {
                        quarterMap[label] = Pair(current.first, current.second + item.transaction.amount)
                    }
                }
                quarterLabels.map { label -> label to (quarterMap[label] ?: Pair(0L, 0L)) }
            }
        }

        // Find max value across all income and expense to normalize bar heights
        var maxAmount = 0L
        groups.forEach { (_, pair) ->
            if (pair.first > maxAmount) maxAmount = pair.first
            if (pair.second > maxAmount) maxAmount = pair.second
        }

        return groups.map { (label, pair) ->
            val income = pair.first
            val expense = pair.second
            val incomeFraction = if (maxAmount > 0) (income.toFloat() / maxAmount.toFloat()).coerceIn(0.05f, 1f) else 0f
            val expenseFraction = if (maxAmount > 0) (expense.toFloat() / maxAmount.toFloat()).coerceIn(0.05f, 1f) else 0f

            BarChartGroup(
                label = label,
                incomeAmount = income,
                expenseAmount = expense,
                incomeFraction = if (income > 0) incomeFraction else 0f,
                expenseFraction = if (expense > 0) expenseFraction else 0f
            )
        }
    }

    private fun formatCurrency(amount: Long, isIncome: Boolean = false, isExpense: Boolean = false): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        val formatted = formatter.format(amount)
        return when {
            isIncome -> "+$formatted ₫"
            isExpense -> "-$formatted ₫"
            else -> "$formatted ₫"
        }
    }
}
