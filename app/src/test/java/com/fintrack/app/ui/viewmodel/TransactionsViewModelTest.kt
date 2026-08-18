package com.fintrack.app.ui.viewmodel

import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.ui.screens.transactions.PeriodFilterMode
import com.fintrack.app.ui.screens.transactions.TransactionFilter
import com.fintrack.app.ui.screens.transactions.TransactionsViewModel
import com.fintrack.app.ui.util.toEpochMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var viewModel: TransactionsViewModel

    @Before
    fun setup() {
        transactionRepository = FakeTransactionRepository()
        preferencesRepository = FakePreferencesRepository()
        viewModel = TransactionsViewModel(
            transactionRepository = transactionRepository,
            preferencesRepository = preferencesRepository
        )
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

    @Test
    fun onPreviousPeriod_and_onNextPeriod_navigateMonths() = runTest {
        val currentYearMonth = YearMonth.now()
        val prevYearMonth = currentYearMonth.minusMonths(1)

        val targetDatePrev = prevYearMonth.atDay(15).atTime(12, 0).toEpochMillis()
        val targetDateCurrent = currentYearMonth.atDay(15).atTime(12, 0).toEpochMillis()

        transactionRepository.addTransaction(
            TransactionEntity(
                id = 1L,
                amount = 50000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Tháng trước",
                transactionDate = targetDatePrev
            )
        )
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 2L,
                amount = 100000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Tháng này",
                transactionDate = targetDateCurrent
            )
        )

        // Initial: default is this month
        val initial = viewModel.uiState.first { it.allTransactions.size == 2 && it.filteredTransactions.size == 1 }
        assertEquals(1, initial.filteredTransactions.size)
        assertEquals("Tháng này", initial.filteredTransactions[0].transaction.note)

        // Navigate previous month
        viewModel.onPreviousPeriod()
        val prev = viewModel.uiState.first { it.periodState.selectedYearMonth == prevYearMonth && it.filteredTransactions.size == 1 }
        assertEquals("Tháng trước", prev.filteredTransactions[0].transaction.note)

        // Navigate next month back to current
        viewModel.onNextPeriod()
        val next = viewModel.uiState.first { it.periodState.selectedYearMonth == currentYearMonth && it.filteredTransactions.size == 1 }
        assertEquals("Tháng này", next.filteredTransactions[0].transaction.note)
    }

    @Test
    fun onSelectToday_filtersTodayTransactions() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        transactionRepository.addTransaction(
            TransactionEntity(
                id = 1L,
                amount = 50000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Hôm nay",
                transactionDate = today.atTime(10, 0).toEpochMillis()
            )
        )
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 2L,
                amount = 100000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Hôm qua",
                transactionDate = yesterday.atTime(10, 0).toEpochMillis()
            )
        )

        viewModel.onSelectToday()
        val state = viewModel.uiState.first { it.periodState.mode == PeriodFilterMode.DAY && it.filteredTransactions.size == 1 }

        assertEquals(1, state.filteredTransactions.size)
        assertEquals("Hôm nay", state.filteredTransactions[0].transaction.note)
    }

    @Test
    fun onSpecificDateSelected_filtersCorrectDate() = runTest {
        val targetDate = LocalDate.of(2026, 8, 16)
        val otherDate = LocalDate.of(2026, 8, 10)

        transactionRepository.addTransaction(
            TransactionEntity(
                id = 1L,
                amount = 120000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Giao dịch ngày 16",
                transactionDate = targetDate.atTime(12, 0).toEpochMillis()
            )
        )
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 2L,
                amount = 80000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Giao dịch ngày 10",
                transactionDate = otherDate.atTime(12, 0).toEpochMillis()
            )
        )

        viewModel.onSpecificDateSelected(targetDate)
        val state = viewModel.uiState.first { it.periodState.selectedDate == targetDate && it.filteredTransactions.size == 1 }

        assertEquals(1, state.filteredTransactions.size)
        assertEquals("Giao dịch ngày 16", state.filteredTransactions[0].transaction.note)
    }

    @Test
    fun onDateRangeSelected_and_onSelectAllTime() = runTest {
        val startDate = LocalDate.of(2026, 8, 1)
        val middleDate = LocalDate.of(2026, 8, 10)
        val endDate = LocalDate.of(2026, 8, 15)
        val outsideDate = LocalDate.of(2026, 8, 25)

        transactionRepository.addTransaction(
            TransactionEntity(
                id = 1L,
                amount = 100000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Trong khoảng",
                transactionDate = middleDate.atTime(12, 0).toEpochMillis()
            )
        )
        transactionRepository.addTransaction(
            TransactionEntity(
                id = 2L,
                amount = 200000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                note = "Ngoài khoảng",
                transactionDate = outsideDate.atTime(12, 0).toEpochMillis()
            )
        )

        viewModel.onDateRangeSelected(startDate, endDate)
        val state = viewModel.uiState.first { it.periodState.customStartDate == startDate && it.filteredTransactions.size == 1 }

        assertEquals(1, state.filteredTransactions.size)
        assertEquals("Trong khoảng", state.filteredTransactions[0].transaction.note)

        // Select All Time
        viewModel.onSelectAllTime()
        val allState = viewModel.uiState.first { it.periodState.mode == PeriodFilterMode.ALL_TIME }
        assertEquals(2, allState.filteredTransactions.size)
    }
}
