package com.fintrack.app.ui.viewmodel

import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.ui.screens.detail.DetailUiEvent
import com.fintrack.app.ui.screens.detail.TransactionDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var viewModel: TransactionDetailViewModel

    @Before
    fun setup() {
        transactionRepository = FakeTransactionRepository()
        viewModel = TransactionDetailViewModel(transactionRepository)
    }

    @Test
    fun loadTransaction_withValidId_loadsCorrectDetails() = runTest {
        val id = transactionRepository.addTransaction(
            TransactionEntity(
                id = 0L,
                amount = 120000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                transactionDate = System.currentTimeMillis(),
                note = "Ăn lẩu"
            )
        )

        viewModel.loadTransaction(id)
        val state = viewModel.uiState.value

        assertNotNull(state.transaction)
        assertEquals(id, state.transaction?.transaction?.id)
        assertEquals(120000L, state.transaction?.transaction?.amount ?: 0L)
        assertEquals("Ăn uống", state.transaction?.category?.name)
        assertNull(state.errorMessage)
    }

    @Test
    fun loadTransaction_withInvalidId_setsErrorMessage() = runTest {
        viewModel.loadTransaction(999L)
        val state = viewModel.uiState.value

        assertNull(state.transaction)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun deleteDialog_showAndDismiss_togglesState() {
        viewModel.showDeleteDialog()
        assertTrue(viewModel.uiState.value.showDeleteConfirmDialog)

        viewModel.dismissDeleteDialog()
        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun confirmDelete_deletesTransactionFromRepository_andSendsNavigateBackEvent() = runTest {
        val receivedEvents = mutableListOf<DetailUiEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { receivedEvents.add(it) }
        }

        val id = transactionRepository.addTransaction(
            TransactionEntity(
                id = 0L,
                amount = 50000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                transactionDate = System.currentTimeMillis(),
                note = "Cà phê"
            )
        )

        viewModel.loadTransaction(id)
        viewModel.confirmDelete()

        assertEquals(listOf(DetailUiEvent.NavigateBack), receivedEvents)

        val remaining = transactionRepository.observeTransactions().first()
        assertTrue(remaining.isEmpty())

        job.cancel()
    }
}
