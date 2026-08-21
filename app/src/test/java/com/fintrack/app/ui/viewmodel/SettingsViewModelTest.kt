package com.fintrack.app.ui.viewmodel

import android.app.Application
import android.content.Context
import com.fintrack.app.data.local.preferences.AppThemeConfig
import com.fintrack.app.data.local.preferences.CurrencyConfig
import com.fintrack.app.data.notification.NotificationSender
import com.fintrack.app.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeNotificationSender: FakeNotificationSender
    private lateinit var viewModel: SettingsViewModel

    private class FakeNotificationSender(
        var result: Boolean = true
    ) : NotificationSender {
        var callCount: Int = 0

        override fun showDailyReminderNotification(context: Context): Boolean {
            callCount++
            return result
        }
    }

    @Before
    fun setUp() {
        fakePreferencesRepository = FakePreferencesRepository()
        fakeTransactionRepository = FakeTransactionRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        fakeNotificationSender = FakeNotificationSender()
        viewModel = SettingsViewModel(
            application = Application(),
            preferencesRepository = fakePreferencesRepository,
            transactionRepository = fakeTransactionRepository,
            categoryRepository = fakeCategoryRepository,
            notificationSender = fakeNotificationSender
        )
    }

    @Test
    fun initialState_hasDefaultPreferences() = runTest {
        val state = viewModel.uiState.first()
        assertFalse(state.isDarkTheme)
        assertEquals("VND (₫)", state.currencyDisplayName)
        assertTrue(state.isDailyReminderEnabled)
        assertEquals("20:00", state.reminderTimeFormatted)
        assertEquals(3, state.categories.size)
    }

    @Test
    fun toggleDarkTheme_updatesThemeState() = runTest {
        viewModel.toggleDarkTheme(true)
        val state = viewModel.uiState.first { it.isDarkTheme }
        assertTrue(state.isDarkTheme)
        assertEquals(AppThemeConfig.DARK, state.userPreferences.theme)

        viewModel.toggleDarkTheme(false)
        val stateLight = viewModel.uiState.first { !it.isDarkTheme }
        assertFalse(stateLight.isDarkTheme)
        assertEquals(AppThemeConfig.LIGHT, stateLight.userPreferences.theme)
    }

    @Test
    fun selectCurrency_updatesCurrencyState() = runTest {
        viewModel.selectCurrency(CurrencyConfig.USD)
        val state = viewModel.uiState.first { it.userPreferences.currency == CurrencyConfig.USD }
        assertEquals("USD ($)", state.currencyDisplayName)
        assertEquals(CurrencyConfig.USD, state.userPreferences.currency)
    }

    @Test
    fun toggleReminderAndChangeTime_updatesState() = runTest {
        viewModel.toggleDailyReminder(false)
        val stateDisabled = viewModel.uiState.first { !it.isDailyReminderEnabled }
        assertFalse(stateDisabled.isDailyReminderEnabled)

        viewModel.toggleDailyReminder(true)
        viewModel.setReminderTime(21, 30)
        val stateTime = viewModel.uiState.first { it.reminderTimeFormatted == "21:30" }
        assertTrue(stateTime.isDailyReminderEnabled)
        assertEquals("21:30", stateTime.reminderTimeFormatted)
    }

    @Test
    fun categoryDialog_opensAndDismisses() = runTest {
        viewModel.openCategoryDialog()
        val openState = viewModel.uiState.first { it.showCategoryDialog }
        assertTrue(openState.showCategoryDialog)

        viewModel.dismissCategoryDialog()
        val closedState = viewModel.uiState.first { !it.showCategoryDialog }
        assertFalse(closedState.showCategoryDialog)
    }

    @Test
    fun resetOnboarding_updatesOnboardingCompletedState() = runTest {
        viewModel.confirmResetOnboarding()
        val state = viewModel.uiState.first { !it.userPreferences.isOnboardingCompleted }
        assertFalse(state.userPreferences.isOnboardingCompleted)
        assertEquals("Đã đặt lại trạng thái Onboarding", state.message)
    }

    @Test
    fun triggerTestNotification_whenDelivered_showsSuccessMessage() = runTest {
        fakeNotificationSender.result = true

        viewModel.triggerTestNotification()

        val state = viewModel.uiState.first { it.message != null }
        assertEquals(1, fakeNotificationSender.callCount)
        assertEquals("Đã gửi thông báo nhắc nhở thử nghiệm 📝", state.message)
    }

    @Test
    fun triggerTestNotification_whenPermissionDenied_showsPermissionMessage() = runTest {
        fakeNotificationSender.result = false

        viewModel.triggerTestNotification()

        val state = viewModel.uiState.first { it.message != null }
        assertEquals(1, fakeNotificationSender.callCount)
        assertEquals("Không thể gửi thông báo. Vui lòng cấp quyền thông báo trong cài đặt.", state.message)
    }
}
