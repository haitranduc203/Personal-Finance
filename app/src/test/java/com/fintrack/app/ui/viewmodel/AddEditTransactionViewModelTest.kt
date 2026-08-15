package com.fintrack.app.ui.viewmodel

import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.ui.screens.add_edit.AddEditTransactionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditTransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var viewModel: AddEditTransactionViewModel

    @Before
    fun setup() {
        categoryRepository = FakeCategoryRepository()
        transactionRepository = FakeTransactionRepository()
        viewModel = AddEditTransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository
        )
    }

    @Test
    fun initialState_loadsExpenseCategories_byDefault() = runTest {
        val state = viewModel.uiState.value
        assertEquals(TransactionType.EXPENSE, state.type)
        assertFalse(state.isEditing)
        assertNotNull(state.selectedCategory)
        assertEquals("Ăn uống", state.selectedCategory?.name)
    }

    @Test
    fun onTypeChange_switchesToIncome_andUpdatesCategories() = runTest {
        viewModel.onTypeChange(TransactionType.INCOME)
        val state = viewModel.uiState.value

        assertEquals(TransactionType.INCOME, state.type)
        assertEquals("Tiền lương", state.selectedCategory?.name)
    }

    @Test
    fun onAmountChange_filtersNonNumericCharacters() = runTest {
        viewModel.onAmountChange("500abc00")
        assertEquals("50000", viewModel.uiState.value.amountInput)
    }

    @Test
    fun saveTransaction_withZeroOrEmptyAmount_showsValidationError() = runTest {
        viewModel.onAmountChange("")
        viewModel.saveTransaction()

        val state = viewModel.uiState.value
        assertNotNull(state.amountError)
        assertFalse(state.isSaved)
    }

    @Test
    fun saveTransaction_withValidData_insertsIntoRepository() = runTest {
        viewModel.onAmountChange("150000")
        viewModel.onNoteChange("Ăn tối nhà hàng")
        viewModel.saveTransaction()

        val state = viewModel.uiState.value
        assertNull(state.amountError)
        assertNull(state.categoryError)
        assertTrue(state.isSaved)

        val allTx = transactionRepository.observeTransactions().first()
        assertEquals(1, allTx.size)
        assertEquals(150000L, allTx[0].transaction.amount)
        assertEquals("Ăn tối nhà hàng", allTx[0].transaction.note)
    }

    @Test
    fun initForTransaction_loadsExistingTransactionForEdit() = runTest {
        val epochMillis = LocalDateTime.of(2026, 8, 15, 10, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val existingId = transactionRepository.addTransaction(
            TransactionEntity(
                id = 0L,
                amount = 250000L,
                type = TransactionType.EXPENSE,
                categoryId = 2L,
                transactionDate = epochMillis,
                note = "Mua sắm quần áo"
            )
        )

        viewModel.initForTransaction(existingId)
        val state = viewModel.uiState.value

        assertTrue(state.isEditing)
        assertEquals(existingId, state.transactionId)
        assertEquals("250000", state.amountInput)
        assertEquals("Mua sắm", state.selectedCategory?.name)
        assertEquals("Mua sắm quần áo", state.note)

        // Edit amount and save
        viewModel.onAmountChange("300000")
        viewModel.saveTransaction()

        assertTrue(viewModel.uiState.value.isSaved)
        val updated = transactionRepository.getTransactionById(existingId)
        assertNotNull(updated)
        assertEquals(300000L, updated!!.transaction.amount)
    }
}
