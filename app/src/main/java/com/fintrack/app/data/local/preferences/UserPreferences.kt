package com.fintrack.app.data.local.preferences

import java.util.Locale

/**
 * App theme configurations.
 */
enum class AppThemeConfig(val title: String) {
    SYSTEM("Theo hệ thống"),
    LIGHT("Giao diện sáng"),
    DARK("Giao diện tối")
}

/**
 * Supported currency configurations.
 */
enum class CurrencyConfig(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    VND("VND", "₫", "VND (₫)"),
    USD("USD", "$", "USD ($)"),
    EUR("EUR", "€", "EUR (€)")
}

/**
 * Data class representing all persisted user preferences.
 */
data class UserPreferences(
    val theme: AppThemeConfig = AppThemeConfig.LIGHT,
    val currency: CurrencyConfig = CurrencyConfig.VND,
    val isDailyReminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val isOnboardingCompleted: Boolean = false
) {
    val reminderTimeFormatted: String
        get() = String.format(Locale.ROOT, "%02d:%02d", reminderHour, reminderMinute)
}
