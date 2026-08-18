package com.fintrack.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.model.TransactionWithCategory
import com.fintrack.app.data.repository.TransactionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DetailUiEvent {
    data object NavigateBack : DetailUiEvent
}

/**
 * UI State for Transaction Detail Screen.
 */
data class TransactionDetailUiState(
    val transaction: TransactionWithCategory? = null,
    val isLoading: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel managing detail view and delete operations for a transaction.
 */
class TransactionDetailViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<DetailUiEvent>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()

    private var loadJob: Job? = null

    fun loadTransaction(id: Long) {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = transactionRepository.observeTransactionById(id)
            .onEach { txWithCat ->
                if (txWithCat != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            transaction = txWithCat,
                            errorMessage = null
                        )
                    }
                } else if (!_uiState.value.isDeleting) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Không tìm thấy giao dịch với ID: $id"
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun confirmDelete() {
        val currentTx = _uiState.value.transaction?.transaction ?: return
        _uiState.update { it.copy(isDeleting = true, showDeleteConfirmDialog = false) }
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(currentTx)
                _uiState.update { it.copy(isDeleting = false) }
                _eventChannel.send(DetailUiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = "Lỗi khi xóa giao dịch: ${e.localizedMessage ?: e.message}"
                    )
                }
            }
        }
    }
}
