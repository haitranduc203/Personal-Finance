package com.fintrack.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

/**
 * UI State for Home Dashboard Screen.
 */
data class HomeUiState(
    val balance: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val selectedMonth: String = "Tháng 8, 2026",
    val isLoading: Boolean = false
)

/**
 * ViewModel managing reactive calculations for the Home Dashboard.
 */
class HomeViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = transactionRepository.observeTransactions()
        .map { allTx ->
            val now = LocalDateTime.now()
            val currentYearMonth = YearMonth.from(now)
            val monthTx = allTx.filter { item ->
                val dt = Instant.ofEpochMilli(item.transaction.transactionDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                YearMonth.from(dt) == currentYearMonth
            }

            var income = 0L
            var expense = 0L
            monthTx.forEach { item ->
                if (item.transaction.type == TransactionType.INCOME) {
                    income += item.transaction.amount
                } else {
                    expense += item.transaction.amount
                }
            }

            HomeUiState(
                balance = income - expense,
                totalIncome = income,
                totalExpense = expense,
                recentTransactions = allTx.take(5),
                selectedMonth = "Tháng ${now.monthValue}, ${now.year}",
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )
}
