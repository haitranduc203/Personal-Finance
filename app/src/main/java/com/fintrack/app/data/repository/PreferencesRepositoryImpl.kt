package com.fintrack.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fintrack.app.data.local.preferences.AppThemeConfig
import com.fintrack.app.data.local.preferences.CurrencyConfig
import com.fintrack.app.data.local.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fintrack_user_preferences")

class PreferencesRepositoryImpl(
    private val context: Context
) : PreferencesRepository {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("app_theme")
        val CURRENCY = stringPreferencesKey("app_currency")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeString = preferences[PreferencesKeys.THEME] ?: AppThemeConfig.LIGHT.name
            val theme = try {
                AppThemeConfig.valueOf(themeString)
            } catch (e: Exception) {
                AppThemeConfig.LIGHT
            }

            val currencyString = preferences[PreferencesKeys.CURRENCY] ?: CurrencyConfig.VND.name
            val currency = try {
                CurrencyConfig.valueOf(currencyString)
            } catch (e: Exception) {
                CurrencyConfig.VND
            }

            val isDailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: true
            val reminderHour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 20
            val reminderMinute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0
            val isOnboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: true

            UserPreferences(
                theme = theme,
                currency = currency,
                isDailyReminderEnabled = isDailyReminderEnabled,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                isOnboardingCompleted = isOnboardingCompleted
            )
        }

    override suspend fun setTheme(theme: AppThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    override suspend fun setCurrency(currency: CurrencyConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY] = currency.name
        }
    }

    override suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled
        }
    }

    override suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun resetOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = false
        }
    }

    override suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
