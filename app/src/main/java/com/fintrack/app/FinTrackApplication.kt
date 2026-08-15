package com.fintrack.app

import android.app.Application
import com.fintrack.app.data.local.AppDatabase
import com.fintrack.app.data.notification.NotificationHelper
import com.fintrack.app.data.repository.CategoryRepository
import com.fintrack.app.data.repository.CategoryRepositoryImpl
import com.fintrack.app.data.repository.PreferencesRepository
import com.fintrack.app.data.repository.PreferencesRepositoryImpl
import com.fintrack.app.data.repository.TransactionRepository
import com.fintrack.app.data.repository.TransactionRepositoryImpl
import com.fintrack.app.worker.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Custom Application class maintaining application-level singleton dependencies.
 */
class FinTrackApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(database.transactionDao())
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(this)
    }

    override fun onCreate() {
        super.onCreate()
        // 1. Initialize Notification Channel
        NotificationHelper.createNotificationChannel(this)

        // 2. Schedule Daily Reminder according to saved preferences
        applicationScope.launch {
            try {
                val prefs = preferencesRepository.userPreferencesFlow.first()
                if (prefs.isDailyReminderEnabled) {
                    ReminderScheduler.scheduleReminder(
                        context = this@FinTrackApplication,
                        hour = prefs.reminderHour,
                        minute = prefs.reminderMinute
                    )
                }
            } catch (e: Exception) {
                // Ignore initialization failures
            }
        }
    }
}
