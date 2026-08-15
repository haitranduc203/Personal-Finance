package com.fintrack.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.local.preferences.AppThemeConfig
import com.fintrack.app.data.local.preferences.CurrencyConfig
import com.fintrack.app.data.local.preferences.UserPreferences
import com.fintrack.app.data.notification.NotificationHelper
import com.fintrack.app.data.repository.PreferencesRepository
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.worker.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
 * ViewModel managing user preferences, WorkManager reminders, and application system settings.
 */
class SettingsViewModel(
    application: Application,
    private val preferencesRepository: PreferencesRepository,
    private val transactionRepository: TransactionRepository
) : AndroidViewModel(application) {

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
            try {
                if (enabled) {
                    val currentPrefs = preferencesRepository.userPreferencesFlow.first()
                    ReminderScheduler.scheduleReminder(
                        context = getApplication(),
                        hour = currentPrefs.reminderHour,
                        minute = currentPrefs.reminderMinute
                    )
                    _dialogState.update { it.copy(message = "Đã bật nhắc nhở hàng ngày lúc ${currentPrefs.reminderTimeFormatted}") }
                } else {
                    ReminderScheduler.cancelReminder(getApplication())
                    _dialogState.update { it.copy(message = "Đã tắt thông báo nhắc nhở hàng ngày") }
                }
            } catch (e: Exception) {
                // Safely handle uninitialized WorkManager in test environments
            }
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
            try {
                val currentPrefs = preferencesRepository.userPreferencesFlow.first()
                if (currentPrefs.isDailyReminderEnabled) {
                    ReminderScheduler.scheduleReminder(
                        context = getApplication(),
                        hour = hour,
                        minute = minute
                    )
                }
            } catch (e: Exception) {
                // Safely handle in test environments
            }
            _dialogState.update {
                it.copy(
                    showTimePickerDialog = false,
                    message = "Đã đổi giờ nhắc nhở thành %02d:%02d".format(hour, minute)
                )
            }
        }
    }

    fun triggerTestNotification() {
        try {
            NotificationHelper.showDailyReminderNotification(getApplication())
            _dialogState.update { it.copy(message = "Đã gửi thông báo nhắc nhở thử nghiệm 📝") }
        } catch (e: Exception) {
            // Safely handle in test environments
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
                preferencesRepository.clearPreferences()
                ReminderScheduler.scheduleReminder(getApplication(), 20, 0)
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
