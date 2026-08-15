package com.fintrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.app.data.local.preferences.AppThemeConfig
import com.fintrack.app.data.local.preferences.UserPreferences
import com.fintrack.app.ui.components.FinTrackApp
import com.fintrack.app.ui.theme.FinTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as FinTrackApplication

        setContent {
            val userPreferences by app.preferencesRepository.userPreferencesFlow
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            val isDarkTheme = when (userPreferences.theme) {
                AppThemeConfig.SYSTEM -> isSystemInDarkTheme()
                AppThemeConfig.DARK -> true
                AppThemeConfig.LIGHT -> false
            }

            FinTrackTheme(darkTheme = isDarkTheme) {
                FinTrackApp()
            }
        }
    }
}
