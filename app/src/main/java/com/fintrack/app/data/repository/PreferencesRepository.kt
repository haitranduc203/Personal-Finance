package com.fintrack.app.data.repository

import com.fintrack.app.data.local.preferences.AppThemeConfig
import com.fintrack.app.data.local.preferences.CurrencyConfig
import com.fintrack.app.data.local.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing persistent user settings via DataStore.
 */
interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun setTheme(theme: AppThemeConfig)

    suspend fun setCurrency(currency: CurrencyConfig)

    suspend fun setDailyReminderEnabled(enabled: Boolean)

    suspend fun setReminderTime(hour: Int, minute: Int)

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun resetOnboarding()

    suspend fun clearPreferences()
}
