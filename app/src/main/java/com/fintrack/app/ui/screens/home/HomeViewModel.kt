package com.fintrack.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.local.preferences.CurrencyConfig
import com.fintrack.app.data.repository.PreferencesRepository
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.ui.util.CurrencyFormatter
import com.fintrack.app.ui.util.getMonthBounds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime

/**
 * Visual indicator type for financial insights on Home Dashboard.
 */
enum class InsightIconType {
    SAVING_UP,
    OVERSPENT,
    NO_EXPENSE,
    FIRST_STEPS
}

/**
 * Dynamic financial insight model calculated from actual monthly cash flow.
 */
data class FinancialInsightUi(
    val title: String,
    val description: String,
    val isPositive: Boolean = true,
    val iconType: InsightIconType = InsightIconType.SAVING_UP
)

/**
 * UI State for Home Dashboard Screen.
 */
data class HomeUiState(
    val balance: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val insight: FinancialInsightUi = FinancialInsightUi(
        title = "Sẵn sàng quản lý tài chính",
        description = "Ghi chép giao dịch đầu tiên để theo dõi dòng tiền hiệu quả",
        isPositive = true,
        iconType = InsightIconType.FIRST_STEPS
    ),
    val currency: CurrencyConfig = CurrencyConfig.VND,
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val selectedMonth: String = "Tháng ${LocalDateTime.now().monthValue}, ${LocalDateTime.now().year}",
    val isLoading: Boolean = false
)

/**
 * ViewModel managing reactive calculations for the Home Dashboard.
 */
class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = run {
        val (start, end) = getMonthBounds()
        combine(
            transactionRepository.observeTransactionsByPeriod(start, end),
            transactionRepository.observeRecentTransactions(5),
            preferencesRepository.userPreferencesFlow
        ) { monthTx, recentTx, prefs ->
            var income = 0L
            var expense = 0L
            monthTx.forEach { item ->
                if (item.transaction.type == TransactionType.INCOME) {
                    income += item.transaction.amount
                } else {
                    expense += item.transaction.amount
                }
            }

            // Dynamic calculation of financial insight
            val insight = when {
                income > 0 && expense > 0 -> {
                    if (income >= expense) {
                        val savedPercent = (((income - expense).toDouble() / income.toDouble()) * 100).toInt()
                        FinancialInsightUi(
                            title = "Xu hướng chi tiêu tháng này",
                            description = "Bạn đang giữ lại được $savedPercent% tổng thu nhập",
                            isPositive = true,
                            iconType = InsightIconType.SAVING_UP
                        )
                    } else {
                        val overspent = expense - income
                        val overspentFormatted = CurrencyFormatter.format(overspent, prefs.currency)
                        FinancialInsightUi(
                            title = "Cảnh báo chi tiêu tháng này",
                            description = "Chi tiêu đang vượt quá thu nhập $overspentFormatted",
                            isPositive = false,
                            iconType = InsightIconType.OVERSPENT
                        )
                    }
                }
                income > 0 && expense == 0L -> {
                    FinancialInsightUi(
                        title = "Khởi đầu tài chính tốt",
                        description = "Tháng này bạn đã có thu nhập và chưa có khoản chi nào",
                        isPositive = true,
                        iconType = InsightIconType.NO_EXPENSE
                    )
                }
                income == 0L && expense > 0L -> {
                    val expenseFormatted = CurrencyFormatter.format(expense, prefs.currency)
                    FinancialInsightUi(
                        title = "Tổng chi tiêu tháng này",
                        description = "Bạn đã chi tiêu $expenseFormatted, chưa ghi nhận khoản thu nào",
                        isPositive = false,
                        iconType = InsightIconType.OVERSPENT
                    )
                }
                else -> {
                    FinancialInsightUi(
                        title = "Sẵn sàng quản lý tài chính",
                        description = "Ghi chép giao dịch đầu tiên để theo dõi dòng tiền hiệu quả",
                        isPositive = true,
                        iconType = InsightIconType.FIRST_STEPS
                    )
                }
            }

            val now = LocalDateTime.now()
            HomeUiState(
                balance = income - expense,
                totalIncome = income,
                totalExpense = expense,
                insight = insight,
                currency = prefs.currency,
                recentTransactions = recentTx,
                selectedMonth = "Tháng ${now.monthValue}, ${now.year}",
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )
    }
}
