package com.fintrack.app.ui.viewmodel

import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.ui.screens.statistics.StatisticsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var viewModel: StatisticsViewModel

    @Before
    fun setUp() {
        fakeTransactionRepository = FakeTransactionRepository()
        viewModel = StatisticsViewModel(fakeTransactionRepository)
    }

    @Test
    fun initialState_isEmpty_whenNoTransactions() = runTest {
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(0L, state.totalIncome)
        assertEquals(0L, state.totalExpense)
        assertEquals(0L, state.dailyAverage)
        assertTrue(state.categoryStats.isEmpty())
    }

    @Test
    fun calculations_areCorrect_forCurrentMonth() = runTest {
        val now = LocalDateTime.now()
        val nowMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        fakeTransactionRepository.addTransaction(
            TransactionEntity(
                amount = 15000000L,
                categoryId = 8L, // Tiền lương
                type = TransactionType.INCOME,
                transactionDate = nowMillis,
                note = "Lương"
            )
        )
        fakeTransactionRepository.addTransaction(
            TransactionEntity(
                amount = 300000L,
                categoryId = 1L, // Ăn uống
                type = TransactionType.EXPENSE,
                transactionDate = nowMillis,
                note = "Ăn uống"
            )
        )
        fakeTransactionRepository.addTransaction(
            TransactionEntity(
                amount = 200000L,
                categoryId = 2L, // Mua sắm
                type = TransactionType.EXPENSE,
                transactionDate = nowMillis,
                note = "Mua sắm"
            )
        )

        val state = viewModel.uiState.first { it.totalExpense == 500000L }

        assertEquals(15000000L, state.totalIncome)
        assertEquals(500000L, state.totalExpense)
        assertEquals(2, state.categoryStats.size)

        // Ăn uống (300k / 500k = 60%)
        val topCategory = state.categoryStats[0]
        assertEquals("Ăn uống", topCategory.name)
        assertEquals(300000L, topCategory.totalAmount)
        assertEquals(0.6f, topCategory.percentage, 0.01f)

        // Mua sắm (200k / 500k = 40%)
        val secondCategory = state.categoryStats[1]
        assertEquals("Mua sắm", secondCategory.name)
        assertEquals(200000L, secondCategory.totalAmount)
        assertEquals(0.4f, secondCategory.percentage, 0.01f)
    }

    @Test
    fun selectPeriod_updatesPeriodAndData() = runTest {
        val now = LocalDateTime.now()
        val nowMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        fakeTransactionRepository.addTransaction(
            TransactionEntity(
                amount = 100000L,
                categoryId = 1L,
                type = TransactionType.EXPENSE,
                transactionDate = nowMillis
            )
        )

        viewModel.selectPeriod(0) // Tuần
        val weekState = viewModel.uiState.first { it.selectedPeriodIndex == 0 }
        assertEquals(0, weekState.selectedPeriodIndex)
        assertTrue(weekState.periodTitle.startsWith("Tuần"))
        assertEquals(100000L, weekState.totalExpense)
        assertEquals(7, weekState.barChartGroups.size) // 7 days (T2-CN)

        viewModel.selectPeriod(2) // Năm
        val yearState = viewModel.uiState.first { it.selectedPeriodIndex == 2 }
        assertEquals(2, yearState.selectedPeriodIndex)
        assertTrue(yearState.periodTitle.startsWith("Năm"))
        assertEquals(4, yearState.barChartGroups.size) // 4 quarters
    }
}
