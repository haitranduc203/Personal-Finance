package com.fintrack.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.app.data.local.preferences.AppThemeConfig
import com.fintrack.app.data.local.preferences.UserPreferences
import com.fintrack.app.ui.components.FinTrackApp
import com.fintrack.app.ui.theme.FinTrackTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as FinTrackApplication

        // Request Notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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
