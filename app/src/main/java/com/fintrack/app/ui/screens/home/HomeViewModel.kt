package com.fintrack.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.local.preferences.CurrencyConfig
import com.fintrack.app.data.repository.PreferencesRepository
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.ui.util.getMonthBounds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime

/**
 * UI State for Home Dashboard Screen.
 */
data class HomeUiState(
    val balance: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
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

            val now = LocalDateTime.now()
            HomeUiState(
                balance = income - expense,
                totalIncome = income,
                totalExpense = expense,
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
