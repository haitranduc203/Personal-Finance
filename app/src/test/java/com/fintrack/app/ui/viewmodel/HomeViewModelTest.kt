package com.fintrack.app.ui.viewmodel

import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.ui.screens.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        fakeTransactionRepository = FakeTransactionRepository()
        viewModel = HomeViewModel(fakeTransactionRepository)
    }

    @Test
    fun initialEmptyState_calculatesZeroBalances() = runTest {
        val state = viewModel.uiState.first { !it.isLoading }
        assertFalse(state.isLoading)
        assertEquals(0L, state.balance)
        assertEquals(0L, state.totalIncome)
        assertEquals(0L, state.totalExpense)
        assertEquals(0, state.recentTransactions.size)
    }

    @Test
    fun withTransactions_calculatesMonthlyBalanceAndRecentList() = runTest {
        val now = System.currentTimeMillis()
        fakeTransactionRepository.addTransaction(
            TransactionEntity(
                id = 1L,
                amount = 20000000L,
                type = TransactionType.INCOME,
                categoryId = 8L,
                note = "Lương tháng này",
                transactionDate = now
            )
        )
        fakeTransactionRepository.addTransaction(
            TransactionEntity(
                id = 2L,
                amount = 500000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Ăn tối",
                transactionDate = now
            )
        )
        fakeTransactionRepository.addTransaction(
            TransactionEntity(
                id = 3L,
                amount = 1500000L,
                type = TransactionType.EXPENSE,
                categoryId = 2L,
                note = "Mua quần áo",
                transactionDate = now
            )
        )

        val state = viewModel.uiState.first { it.recentTransactions.isNotEmpty() }
        assertEquals(20000000L, state.totalIncome)
        assertEquals(2000000L, state.totalExpense)
        assertEquals(18000000L, state.balance)
        assertEquals(3, state.recentTransactions.size)
        assertEquals("Ăn uống", state.recentTransactions[1].category.name)
    }
}
