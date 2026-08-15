package com.fintrack.app.ui.viewmodel

import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.ui.screens.transactions.TransactionFilter
import com.fintrack.app.ui.screens.transactions.TransactionsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var viewModel: TransactionsViewModel

    @Before
    fun setup() {
        transactionRepository = FakeTransactionRepository()
        viewModel = TransactionsViewModel(transactionRepository)
    }

    @Test
    fun initialState_computesSummaryAndFilteredList() = runTest {
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 1L,
                amount = 50000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Cà phê",
                transactionDate = System.currentTimeMillis()
            )
        )
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 2L,
                amount = 15000000L,
                type = TransactionType.INCOME,
                categoryId = 8L,
                note = "Lương",
                transactionDate = System.currentTimeMillis()
            )
        )

        val state = viewModel.uiState.first { it.allTransactions.size == 2 }
        assertEquals(2, state.totalCount)
        assertEquals(15000000L, state.totalIncome)
        assertEquals(50000L, state.totalExpense)
        assertEquals(2, state.filteredTransactions.size)
    }

    @Test
    fun onFilterSelected_filtersExpenseOnly() = runTest {
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 1L,
                amount = 50000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Cà phê",
                transactionDate = System.currentTimeMillis()
            )
        )
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 2L,
                amount = 15000000L,
                type = TransactionType.INCOME,
                categoryId = 8L,
                note = "Lương",
                transactionDate = System.currentTimeMillis()
            )
        )

        viewModel.onFilterSelected(TransactionFilter.EXPENSE)
        val state = viewModel.uiState.first { it.selectedFilter == TransactionFilter.EXPENSE && it.filteredTransactions.size == 1 }

        assertEquals(1, state.filteredTransactions.size)
        assertEquals("Cà phê", state.filteredTransactions[0].transaction.note)
    }

    @Test
    fun onSearchQueryChange_filtersByKeyword() = runTest {
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 1L,
                amount = 50000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Cà phê sáng",
                transactionDate = System.currentTimeMillis()
            )
        )
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 2L,
                amount = 70000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Ăn bún bò",
                transactionDate = System.currentTimeMillis()
            )
        )

        viewModel.onSearchQueryChange("bún")
        val state = viewModel.uiState.first { it.searchQuery == "bún" && it.filteredTransactions.size == 1 }

        assertEquals(1, state.filteredTransactions.size)
        assertEquals("Ăn bún bò", state.filteredTransactions[0].transaction.note)
    }
}
