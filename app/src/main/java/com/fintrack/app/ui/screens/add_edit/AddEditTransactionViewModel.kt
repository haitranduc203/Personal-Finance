package com.fintrack.app.ui.screens.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.entity.CategoryEntity
import com.fintrack.app.data.local.entity.TransactionEntity
import com.fintrack.app.data.local.model.CategoryType
import com.fintrack.app.data.local.model.TransactionType
import com.fintrack.app.data.repository.CategoryRepository
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.ui.util.toEpochMillis
import com.fintrack.app.ui.util.toLocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed interface AddEditUiEvent {
    data object NavigateBack : AddEditUiEvent
    data class ShowError(val message: String) : AddEditUiEvent
}

data class AddEditTransactionUiState(
    val transactionId: Long? = null,
    val isEditing: Boolean = false,
    val amountInput: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: CategoryEntity? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val note: String = "",
    val amountError: String? = null,
    val categoryError: String? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val generalError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTransactionUiState())
    val uiState: StateFlow<AddEditTransactionUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<AddEditUiEvent>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()

    private val _selectedType = MutableStateFlow(TransactionType.EXPENSE)

    init {
        _selectedType
            .flatMapLatest { type ->
                val catType = if (type == TransactionType.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
                categoryRepository.observeCategoriesByType(catType)
            }
            .onEach { categoriesList ->
                _uiState.update { state ->
                    val currentSelected = state.selectedCategory
                    val newSelected = if (currentSelected != null &&
                        categoriesList.any { it.id == currentSelected.id }
                    ) currentSelected else categoriesList.firstOrNull()
                    state.copy(categories = categoriesList, selectedCategory = newSelected, categoryError = null)
                }
            }
            .launchIn(viewModelScope)
    }

    fun initForTransaction(id: Long?) {
        if (id == null || id <= 0) {
            _uiState.update { it.copy(isEditing = false, transactionId = null) }
            return
        }
        _uiState.update { it.copy(isLoading = true, isEditing = true, transactionId = id) }
        viewModelScope.launch {
            val txWithCat = transactionRepository.getTransactionById(id)
            if (txWithCat != null) {
                val dt = txWithCat.transaction.transactionDate.toLocalDateTime()
                _selectedType.value = txWithCat.transaction.type
                _uiState.update {
                    it.copy(
                        isLoading = false, isEditing = true, transactionId = id,
                        amountInput = txWithCat.transaction.amount.toString(),
                        type = txWithCat.transaction.type,
                        selectedCategory = txWithCat.category,
                        dateTime = dt,
                        note = txWithCat.transaction.note ?: ""
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, generalError = "Không tìm thấy giao dịch") }
            }
        }
    }

    fun onAmountChange(input: String) {
        val cleanInput = input.filter { it.isDigit() }
        _uiState.update {
            it.copy(
                amountInput = cleanInput,
                amountError = if (cleanInput.isNotEmpty() && (cleanInput.toLongOrNull() ?: 0L) > 0L) null else it.amountError
            )
        }
    }

    fun onTypeChange(type: TransactionType) {
        if (_uiState.value.type != type) {
            _selectedType.value = type
            _uiState.update { it.copy(type = type) }
        }
    }

    fun onCategorySelect(category: CategoryEntity) {
        _uiState.update { it.copy(selectedCategory = category, categoryError = null) }
    }

    fun onDateTimeChange(dateTime: LocalDateTime) {
        _uiState.update { it.copy(dateTime = dateTime) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val amount = state.amountInput.toLongOrNull()
        var hasError = false
        var amountError: String? = null
        var categoryError: String? = null

        if (amount == null || amount <= 0L) {
            amountError = "Vui lòng nhập số tiền hợp lệ (> 0)"
            hasError = true
        }
        if (state.selectedCategory == null) {
            categoryError = "Vui lòng chọn một danh mục"
            hasError = true
        }
        if (hasError) {
            _uiState.update { it.copy(amountError = amountError, categoryError = categoryError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, amountError = null, categoryError = null) }
        viewModelScope.launch {
            try {
                val epochMillis = state.dateTime.toEpochMillis()
                val entity = TransactionEntity(
                    id = state.transactionId ?: 0L,
                    amount = amount!!,
                    type = state.type,
                    categoryId = state.selectedCategory!!.id,
                    transactionDate = epochMillis,
                    note = state.note.trim().ifEmpty { null }
                )
                if (state.isEditing && state.transactionId != null) {
                    transactionRepository.updateTransaction(entity)
                } else {
                    transactionRepository.addTransaction(entity)
                }
                _uiState.update { it.copy(isSubmitting = false) }
                _eventChannel.send(AddEditUiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, generalError = "Có lỗi xảy ra: ${e.localizedMessage ?: e.message}")
                }
            }
        }
    }
}
