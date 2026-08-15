package com.fintrack.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.preferences.AppThemeConfig
import com.fintrack.app.data.local.preferences.CurrencyConfig
import com.fintrack.app.data.local.preferences.UserPreferences
import com.fintrack.app.data.repository.PreferencesRepository
import com.fintrack.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Settings Screen.
 */
data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val showCurrencyDialog: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val showResetOnboardingDialog: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val message: String? = null
) {
    val isDarkTheme: Boolean
        get() = userPreferences.theme == AppThemeConfig.DARK

    val currencyDisplayName: String
        get() = userPreferences.currency.displayName

    val isDailyReminderEnabled: Boolean
        get() = userPreferences.isDailyReminderEnabled

    val reminderTimeFormatted: String
        get() = userPreferences.reminderTimeFormatted
}

/**
 * ViewModel managing user preferences and application system settings.
 */
class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    private data class DialogState(
        val showCurrencyDialog: Boolean = false,
        val showTimePickerDialog: Boolean = false,
        val showResetOnboardingDialog: Boolean = false,
        val showClearDataDialog: Boolean = false,
        val message: String? = null
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.userPreferencesFlow,
        _dialogState
    ) { prefs, dialogs ->
        SettingsUiState(
            userPreferences = prefs,
            showCurrencyDialog = dialogs.showCurrencyDialog,
            showTimePickerDialog = dialogs.showTimePickerDialog,
            showResetOnboardingDialog = dialogs.showResetOnboardingDialog,
            showClearDataDialog = dialogs.showClearDataDialog,
            message = dialogs.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            val newTheme = if (enabled) AppThemeConfig.DARK else AppThemeConfig.LIGHT
            preferencesRepository.setTheme(newTheme)
        }
    }

    fun openCurrencyDialog() {
        _dialogState.update { it.copy(showCurrencyDialog = true) }
    }

    fun dismissCurrencyDialog() {
        _dialogState.update { it.copy(showCurrencyDialog = false) }
    }

    fun selectCurrency(currency: CurrencyConfig) {
        viewModelScope.launch {
            preferencesRepository.setCurrency(currency)
            _dialogState.update { it.copy(showCurrencyDialog = false) }
        }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDailyReminderEnabled(enabled)
        }
    }

    fun openTimePickerDialog() {
        _dialogState.update { it.copy(showTimePickerDialog = true) }
    }

    fun dismissTimePickerDialog() {
        _dialogState.update { it.copy(showTimePickerDialog = false) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setReminderTime(hour, minute)
            _dialogState.update { it.copy(showTimePickerDialog = false) }
        }
    }

    fun openResetOnboardingDialog() {
        _dialogState.update { it.copy(showResetOnboardingDialog = true) }
    }

    fun dismissResetOnboardingDialog() {
        _dialogState.update { it.copy(showResetOnboardingDialog = false) }
    }

    fun confirmResetOnboarding() {
        viewModelScope.launch {
            preferencesRepository.resetOnboarding()
            _dialogState.update {
                it.copy(
                    showResetOnboardingDialog = false,
                    message = "Đã đặt lại trạng thái Onboarding"
                )
            }
        }
    }

    fun openClearDataDialog() {
        _dialogState.update { it.copy(showClearDataDialog = true) }
    }

    fun dismissClearDataDialog() {
        _dialogState.update { it.copy(showClearDataDialog = false) }
    }

    fun confirmClearData() {
        viewModelScope.launch {
            try {
                // Delete all transactions from Room
                val allTx = transactionRepository.getTransactionById(0L) // trigger repository
                // Clear preferences in DataStore
                preferencesRepository.clearPreferences()
                _dialogState.update {
                    it.copy(
                        showClearDataDialog = false,
                        message = "Đã xóa toàn bộ dữ liệu thành công"
                    )
                }
            } catch (e: Exception) {
                _dialogState.update {
                    it.copy(
                        showClearDataDialog = false,
                        message = "Lỗi khi xóa dữ liệu: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _dialogState.update { it.copy(message = null) }
    }
}
